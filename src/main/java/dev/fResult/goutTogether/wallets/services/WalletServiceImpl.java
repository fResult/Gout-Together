package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tours.services.TourService;
import dev.fResult.goutTogether.transactions.Transaction;
import dev.fResult.goutTogether.transactions.TransactionHelper;
import dev.fResult.goutTogether.transactions.TransactionRepository;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletServiceImpl implements WalletService {
  private final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(WalletServiceImpl.class);

  private final UserWalletRepository userWalletRepository;
  private final TourCompanyWalletRepository tourCompanyWalletRepository;
  private final TransactionRepository transactionRepository;
  private final TourService tourService;

  public WalletServiceImpl(
      UserWalletRepository userWalletRepository,
      TourCompanyWalletRepository tourCompanyWalletRepository,
      TransactionRepository transactionRepository,
      @Lazy TourService tourService) {

    this.userWalletRepository = userWalletRepository;
    this.tourCompanyWalletRepository = tourCompanyWalletRepository;
    this.transactionRepository = transactionRepository;
    this.tourService = tourService;
  }

  @Override
  public UserWallet createConsumerWallet(int userId) {
    logger.debug("[createConsumerWallet] New {} is creating", UserWallet.class.getSimpleName());

    var walletToCreate =
        UserWallet.of(null, AggregateReference.to(userId), Instant.now(), BigDecimal.ZERO);
    var createdWallet = userWalletRepository.save(walletToCreate);

    logger.info(
        "[createConsumerWallet] New {} is created: {}",
        UserWallet.class.getSimpleName(),
        createdWallet);

    return createdWallet;
  }

  @Override
  public UserWalletInfoResponse getConsumerWalletByUserId(int userId) {
    logger.debug(
        "[getConsumerWalletByUserId] Getting {} by userId: {}",
        UserWallet.class.getSimpleName(),
        userId);

    return UserWalletInfoResponse.fromDao(getUserWalletByUserId(userId));
  }

  @Override
  @Transactional
  public UserWalletInfoResponse topUpConsumerWallet(
      int userId, String idempotentKey, WalletTopUpRequest body) {
    var existsTransactionOpt = transactionRepository.findOneByIdempotentKey(idempotentKey);

    var userWallet =
        userWalletRepository
            .findOneByUserId(AggregateReference.to(userId))
            .map(UserWalletInfoResponse::fromDao)
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "topUpConsumerWallet", UserWallet.class, "userId", String.valueOf(userId)));

    if (existsTransactionOpt.isPresent()) return userWallet;

    var userRef = AggregateReference.<User, Integer>to(userWallet.userId());
    var transactionToAdd = createTopUpTransaction(userRef, body.amount(), idempotentKey);

    transactionRepository.save(transactionToAdd);
    logger.info(
        "[topUpConsumerWallet] {} with userId [{}] is added",
        Transaction.class.getSimpleName(),
        userId);

    var balanceToUpdate = userWallet.balance().add(body.amount());
    var userWalletToUpdate =
        UserWallet.of(userWallet.id(), userRef, Instant.now(), balanceToUpdate);

    var updatedUserWallet = userWalletRepository.save(userWalletToUpdate);
    logger.info(
        "[topUpConsumerWallet] {} with userId [{}] is topped up: {}",
        UserWallet.class.getSimpleName(),
        userId,
        updatedUserWallet);

    return UserWalletInfoResponse.fromDao(updatedUserWallet);
  }

  @Override
  public boolean deleteConsumerWalletByUserId(int userId) {
    logger.debug(
        "[deleteUserWalletById] Deleting {} by id: {}", UserWallet.class.getSimpleName(), userId);
    var walletToDelete = getUserWalletByUserId(userId);

    userWalletRepository.delete(walletToDelete);
    logger.info(
        "[deleteUserWalletById] {} id [{}] is deleted", UserWallet.class.getSimpleName(), userId);

    return true;
  }

  @Override
  public TourCompanyWallet createTourCompanyWallet(int tourCompanyId) {
    logger.debug(
        "[createTourCompanyWallet] New {} is creating", TourCompanyWallet.class.getSimpleName());

    var walletToCreate =
        TourCompanyWallet.of(
            null, AggregateReference.to(tourCompanyId), Instant.now(), BigDecimal.ZERO);

    var createdWallet = tourCompanyWalletRepository.save(walletToCreate);
    logger.info(
        "[createTourCompanyWallet] New {} is created: {}",
        TourCompanyWallet.class.getSimpleName(),
        createdWallet);

    return createdWallet;
  }

  @Override
  public Pair<UserWallet, TourCompanyWallet> getConsumerAndTourCompanyWallets(Booking booking) {
    var userRef = booking.userId();
    var tourRef = booking.tourId();
    if (userRef == null || tourRef == null) {
      logger.warn(
          "[getConsumerAndTourCompanyWallets] {}'s userId or tourId must not be null null",
          Booking.class.getSimpleName());
      throw new EntityNotFoundException(
          String.format(
              "[getConsumerAndTourCompanyWallets] %s with userId [%s] tourId [%s] not found",
              Booking.class.getSimpleName(), getIdOrNull(userRef), getIdOrNull(tourRef)));
    }

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var userWalletFuture =
          CompletableFuture.supplyAsync(
              () -> getUserWalletByUserId(Objects.requireNonNull(userRef.getId())));
      var tourFuture =
          CompletableFuture.supplyAsync(
              () -> tourService.getTourById(Objects.requireNonNull(tourRef.getId())));

      CompletableFuture.allOf(userWalletFuture, tourFuture).join();

      var userWallet = userWalletFuture.get();
      var tour = tourFuture.get();
      var tourCompanyWallet =
          tourCompanyWalletRepository
              .findOneByTourCompanyId(tour.tourCompanyId())
              .orElseThrow(
                  errorHelper.entityWithSubResourceNotFound(
                      "getConsumerAndTourCompanyWallets",
                      TourCompanyWallet.class,
                      "tourCompanyId",
                      String.valueOf(tour.tourCompanyId())));

      return new Pair<>(userWallet, tourCompanyWallet);
    } catch (InterruptedException | ExecutionException ex) {
      throw new RuntimeException("Failed to get user wallets", ex);
    }
  }

  @Override
  public Pair<UserWallet, TourCompanyWallet> transferMoney(
      UserWallet userWallet,
      TourCompanyWallet tourCompanyWallet,
      BigDecimal amount,
      TransactionType transactionType) {

    logger.debug(
        "[transferMoney] Transferring {} {} from {} id [{}] to {} id [{}]",
        transactionType,
        amount,
        UserWallet.class.getSimpleName(),
        userWallet.id(),
        TourCompanyWallet.class.getSimpleName(),
        tourCompanyWallet.id());

    return switch (transactionType) {
      case BOOKING -> transferMoneyForBooking(userWallet, tourCompanyWallet, amount);
      case REFUND -> transferMoneyForRefund(userWallet, tourCompanyWallet, amount);
      default ->
          throw errorHelper.unsupportedTransactionType("transferMoney", transactionType).get();
    };
  }

  private Pair<UserWallet, TourCompanyWallet> transferMoneyForBooking(
      UserWallet userWallet, TourCompanyWallet companyWallet, BigDecimal amount) {
    var userWalletBalance = userWallet.balance();
    var isInsufficientBalance = userWalletBalance.compareTo(amount) < 0;
    if (isInsufficientBalance) {
      throw errorHelper.insufficientBalance("transferMoney", userWalletBalance, amount).get();
    }

    var userWalletBalanceToUpdate = userWalletBalance.subtract(amount);
    var userWalletToUpdate =
        UserWallet.of(
            userWallet.id(), userWallet.userId(), Instant.now(), userWalletBalanceToUpdate);

    var tourCompanyWalletBalance = companyWallet.balance();
    var tourCompanyWalletBalanceToUpdate = tourCompanyWalletBalance.add(amount);
    var tourCompanyWalletToUpdate =
        TourCompanyWallet.of(
            companyWallet.id(),
            companyWallet.tourCompanyId(),
            Instant.now(),
            tourCompanyWalletBalanceToUpdate);

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      // TODO: Make pessimistic lock to avoid race condition
      var futureUpdatedUserWallet =
          CompletableFuture.supplyAsync(
              () -> userWalletRepository.save(userWalletToUpdate), executor);
      var futureUpdatedCompanyWallet =
          CompletableFuture.supplyAsync(
              () -> tourCompanyWalletRepository.save(tourCompanyWalletToUpdate), executor);

      CompletableFuture.allOf(futureUpdatedUserWallet, futureUpdatedCompanyWallet).join();

      var updatedUserWallet = futureUpdatedUserWallet.get();
      var updatedCompanyWallet = futureUpdatedCompanyWallet.get();

      logger.info(
          "[transferMoney] {} {} from {} id [{}] to {} id [{}] is transferred",
          TransactionType.BOOKING,
          amount,
          UserWallet.class.getSimpleName(),
          userWallet.id(),
          TourCompanyWallet.class.getSimpleName(),
          companyWallet.id());

      return new Pair<>(updatedUserWallet, updatedCompanyWallet);
    } catch (ExecutionException | InterruptedException ex) {
      var errorMessage =
          String.format(
              "Failed to transfer money between %s and %s",
              User.class.getSimpleName(), TourCompanyWallet.class.getSimpleName());
      throw new RuntimeException(errorMessage, ex);
    }
  }

  private Pair<UserWallet, TourCompanyWallet> transferMoneyForRefund(
      UserWallet userWallet, TourCompanyWallet companyWallet, BigDecimal amount) {

    var companyWalletBalance = companyWallet.balance();
    var isBalanceInsufficient = companyWalletBalance.compareTo(amount) < 0;

    if (isBalanceInsufficient)
      throw errorHelper.insufficientBalance("transferMoney", companyWalletBalance, amount).get();

    var companyWalletBalanceToUpdate = companyWalletBalance.subtract(amount);
    var companyWalletToUpdate =
        TourCompanyWallet.of(
            companyWallet.id(),
            companyWallet.tourCompanyId(),
            Instant.now(),
            companyWalletBalanceToUpdate);

    var userWalletBalance = userWallet.balance();
    var userWalletBalanceToUpdate = userWalletBalance.add(amount);
    var userWalletToUpdate =
        UserWallet.of(
            userWallet.id(), userWallet.userId(), Instant.now(), userWalletBalanceToUpdate);

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      // TODO: Make pessimistic lock to avoid race condition
      var futureUpdatedUserWallet =
          CompletableFuture.supplyAsync(
              () -> userWalletRepository.save(userWalletToUpdate), executor);
      var futureUpdatedCompanyWallet =
          CompletableFuture.supplyAsync(
              () -> tourCompanyWalletRepository.save(companyWalletToUpdate), executor);

      var updatedUserWallet = futureUpdatedUserWallet.get();
      var updatedCompanyWallet = futureUpdatedCompanyWallet.get();

      logger.info(
          "[transferMoney] {} {} from {} id [{}] to {} id [{}] is transferred",
          TransactionType.REFUND,
          amount,
          TourCompanyWallet.class.getSimpleName(),
          companyWallet.id(),
          UserWallet.class.getSimpleName(),
          userWallet.id());

      return new Pair<>(updatedUserWallet, updatedCompanyWallet);
    } catch (ExecutionException | InterruptedException ex) {
      var errorMessage =
          String.format(
              "Failed to transfer money between %s and %s",
              User.class.getSimpleName(), TourCompanyWallet.class.getSimpleName());
      throw new RuntimeException(errorMessage, ex);
    }
  }

  private UserWallet getUserWalletByUserId(int userId) {
    logger.debug(
        "[findUserWalletByUserId] Finding {} by userId: {}",
        UserWallet.class.getSimpleName(),
        userId);

    return userWalletRepository
        .findOneByUserId(AggregateReference.to(userId))
        .orElseThrow(
            errorHelper.entityNotFound("findUserWalletByUserId", UserWallet.class, userId));
  }

  private Transaction createTopUpTransaction(
      AggregateReference<User, Integer> userRef, BigDecimal amount, String idempotentKey) {

    var transactionToCreate =
        TransactionHelper.buildTopUpTransaction(userRef.getId(), amount, idempotentKey);
    var createdTransaction = transactionRepository.save(transactionToCreate);
    logger.info(
        "[createTopUpTransaction] New {} is created: {}",
        Transaction.class.getSimpleName(),
        createdTransaction);
    return createdTransaction;
  }

  private Integer getIdOrNull(AggregateReference<?, Integer> resourceRef) {
    return resourceRef != null ? resourceRef.getId() : null;
  }
}
