package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.helpers.ErrorHelper;
import dev.fResult.goutTogether.tours.services.TourService;
import dev.fResult.goutTogether.transactions.Transaction;
import dev.fResult.goutTogether.transactions.TransactionHelper;
import dev.fResult.goutTogether.transactions.TransactionRepository;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.dtos.TourCompanyWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.dtos.WalletWithdrawRequest;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

    final var walletToCreate =
        UserWallet.of(null, AggregateReference.to(userId), Instant.now(), BigDecimal.ZERO);
    final var createdWallet = userWalletRepository.save(walletToCreate);

    logger.info(
        "[createConsumerWallet] New {} is created: {}",
        UserWallet.class.getSimpleName(),
        createdWallet);

    return createdWallet;
  }

  @Override
  public UserWalletInfoResponse getConsumerWalletInfoByUserId(int userId) {
    logger.debug(
        "[getConsumerWalletInfoByUserId] Getting {} by userId: {}",
        UserWallet.class.getSimpleName(),
        userId);

    return UserWalletInfoResponse.fromDao(getUserWalletByUserId(userId));
  }

  @Override
  @Transactional
  public UserWalletInfoResponse topUpConsumerWallet(
      int userId, String idempotentKey, WalletTopUpRequest body) {
    final var existsTransactionOpt = transactionRepository.findOneByIdempotentKey(idempotentKey);

    final var userWallet =
        userWalletRepository
            .findOneByUserId(AggregateReference.to(userId))
            .map(UserWalletInfoResponse::fromDao)
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "topUpConsumerWallet", UserWallet.class, "userId", String.valueOf(userId)));

    if (existsTransactionOpt.isPresent()) return userWallet;

    final var userRef = AggregateReference.<User, Integer>to(userWallet.userId());
    final var transactionToAdd = createTopUpTransaction(userRef, body.amount(), idempotentKey);

    transactionRepository.save(transactionToAdd);
    logger.info(
        "[topUpConsumerWallet] {} with userId [{}] is added",
        Transaction.class.getSimpleName(),
        userId);

    final var balanceToUpdate = userWallet.balance().add(body.amount());
    final var userWalletToUpdate =
        UserWallet.of(userWallet.id(), userRef, Instant.now(), balanceToUpdate);

    final var updatedUserWallet = userWalletRepository.save(userWalletToUpdate);
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
    final var walletToDelete = getUserWalletByUserId(userId);

    userWalletRepository.delete(walletToDelete);
    logger.info(
        "[deleteUserWalletById] {} id [{}] is deleted", UserWallet.class.getSimpleName(), userId);

    return true;
  }

  @Override
  public TourCompanyWallet createTourCompanyWallet(int tourCompanyId) {
    logger.debug(
        "[createTourCompanyWallet] New {} is creating", TourCompanyWallet.class.getSimpleName());

    final var walletToCreate =
        TourCompanyWallet.of(
            null, AggregateReference.to(tourCompanyId), Instant.now(), BigDecimal.ZERO);

    final var createdWallet = tourCompanyWalletRepository.save(walletToCreate);
    logger.info(
        "[createTourCompanyWallet] New {} is created: {}",
        TourCompanyWallet.class.getSimpleName(),
        createdWallet);

    return createdWallet;
  }

  @Override
  public TourCompanyWalletInfoResponse getTourCompanyWalletInfoByTourCompanyId(int tourCompanyId) {
    logger.debug(
        "[getTourCompanyWalletInfoByTourCompanyId] Getting {} by tourCompanyId: {}",
        TourCompanyWallet.class.getSimpleName(),
        tourCompanyId);
    final var tourCompanyWallet = getTourCompanyWalletByTourCompanyId(tourCompanyId);

    return TourCompanyWalletInfoResponse.fromDao(tourCompanyWallet);
  }

  @Override
  public TourCompanyWalletInfoResponse withdrawTourCompanyWallet(
      int tourCompanyId, String idempotentKey, WalletWithdrawRequest body) {

    logger.debug(
        "[withdrawTourCompanyWallet] Withdrawing {} by tourCompanyId: {}",
        TourCompanyWallet.class.getSimpleName(),
        tourCompanyId);

    final var companyWallet = getTourCompanyWalletByTourCompanyId(tourCompanyId);
    final var balanceToWithdraw = companyWallet.balance().subtract(body.amount());
    final var companyWalletToWithdraw =
        TourCompanyWallet.of(
            companyWallet.id(), companyWallet.tourCompanyId(), Instant.now(), balanceToWithdraw);
    final var withdrewCompanyWallet = tourCompanyWalletRepository.save(companyWalletToWithdraw);

    logger.info(
        "[withdrawTourCompanyWallet] {} is withdrew: {}",
        TourCompanyWallet.class.getSimpleName(),
        withdrewCompanyWallet);

    return TourCompanyWalletInfoResponse.fromDao(withdrewCompanyWallet);
  }

  @Override
  public Pair<UserWallet, TourCompanyWallet> getConsumerAndTourCompanyWallets(Booking booking) {
    final var userRef = booking.userId();
    final var tourRef = booking.tourId();
    if (userRef == null || tourRef == null) {
      logger.warn(
          "[getConsumerAndTourCompanyWallets] {}'s userId or tourId must not be null null",
          Booking.class.getSimpleName());
      throw new EntityNotFoundException(
          String.format(
              "%s with userId [%s] tourId [%s] not found",
              Booking.class.getSimpleName(), getIdOrNull(userRef), getIdOrNull(tourRef)));
    }

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var userWalletFuture =
          CompletableFuture.supplyAsync(
              () -> getUserWalletByUserId(Objects.requireNonNull(userRef.getId())));
      final var tourFuture =
          CompletableFuture.supplyAsync(
              () -> tourService.getTourById(Objects.requireNonNull(tourRef.getId())));

      CompletableFuture.allOf(userWalletFuture, tourFuture).join();

      final var userWallet = userWalletFuture.get();
      final var tour = tourFuture.get();
      final var tourCompanyWallet =
          tourCompanyWalletRepository
              .findOneByTourCompanyId(tour.tourCompanyId())
              .orElseThrow(
                  errorHelper.entityWithSubResourceNotFound(
                      "getConsumerAndTourCompanyWallets",
                      TourCompanyWallet.class,
                      "tourCompanyId",
                      String.valueOf(tour.tourCompanyId())));

      return new Pair<>(userWallet, tourCompanyWallet);
    } catch (CompletionException | ExecutionException ex) {
      throw ErrorHelper.throwMatchedException(ex.getCause(), "Failed to get wallets");
    } catch (InterruptedException ex) {
      throw new RuntimeException("Task interrupted", ex);
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
    final var userWalletBalance = userWallet.balance();
    final var isInsufficientBalance = userWalletBalance.compareTo(amount) < 0;
    if (isInsufficientBalance) {
      throw errorHelper.insufficientBalance("transferMoney", userWalletBalance, amount).get();
    }

    final var userWalletBalanceToUpdate = userWalletBalance.subtract(amount);
    final var userWalletToUpdate =
        UserWallet.of(
            userWallet.id(), userWallet.userId(), Instant.now(), userWalletBalanceToUpdate);

    final var tourCompanyWalletBalance = companyWallet.balance();
    final var tourCompanyWalletBalanceToUpdate = tourCompanyWalletBalance.add(amount);
    final var tourCompanyWalletToUpdate =
        TourCompanyWallet.of(
            companyWallet.id(),
            companyWallet.tourCompanyId(),
            Instant.now(),
            tourCompanyWalletBalanceToUpdate);

    // TODO: Make pessimistic lock to avoid race condition
    final var updatedUserWallet = userWalletRepository.save(userWalletToUpdate);
    final var updatedCompanyWallet = tourCompanyWalletRepository.save(tourCompanyWalletToUpdate);

    logger.info(
        "[transferMoney] {} {} from {} id [{}] to {} id [{}] is transferred",
        TransactionType.BOOKING,
        amount,
        UserWallet.class.getSimpleName(),
        userWallet.id(),
        TourCompanyWallet.class.getSimpleName(),
        companyWallet.id());

    return new Pair<>(updatedUserWallet, updatedCompanyWallet);
  }

  private Pair<UserWallet, TourCompanyWallet> transferMoneyForRefund(
      UserWallet userWallet, TourCompanyWallet companyWallet, BigDecimal amount) {

    final var companyWalletBalance = companyWallet.balance();

    final var companyWalletBalanceToUpdate = companyWalletBalance.subtract(amount);
    final var companyWalletToUpdate =
        TourCompanyWallet.of(
            companyWallet.id(),
            companyWallet.tourCompanyId(),
            Instant.now(),
            companyWalletBalanceToUpdate);

    final var userWalletBalance = userWallet.balance();
    final var userWalletBalanceToUpdate = userWalletBalance.add(amount);
    final var userWalletToUpdate =
        UserWallet.of(
            userWallet.id(), userWallet.userId(), Instant.now(), userWalletBalanceToUpdate);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      // TODO: Make pessimistic lock to avoid race condition
      final var futureUpdatedUserWallet =
          CompletableFuture.supplyAsync(
              () -> userWalletRepository.save(userWalletToUpdate), executor);
      final var futureUpdatedCompanyWallet =
          CompletableFuture.supplyAsync(
              () -> tourCompanyWalletRepository.save(companyWalletToUpdate), executor);

      final var updatedUserWallet = futureUpdatedUserWallet.get();
      final var updatedCompanyWallet = futureUpdatedCompanyWallet.get();

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
      final var errorMessage =
          String.format(
              "Failed to transfer money between %s and %s",
              User.class.getSimpleName(), TourCompanyWallet.class.getSimpleName());

      throw new RuntimeException(errorMessage, ex);
    }
  }

  private UserWallet getUserWalletByUserId(int userId) {
    logger.debug(
        "[getUserWalletByUserId] Getting {} by userId [{}]",
        UserWallet.class.getSimpleName(),
        userId);

    return userWalletRepository
        .findOneByUserId(AggregateReference.to(userId))
        .orElseThrow(
            errorHelper.entityWithSubResourceNotFound(
                "getUserWalletByUserId", UserWallet.class, "userId", String.valueOf(userId)));
  }

  private Transaction createTopUpTransaction(
      AggregateReference<User, Integer> userRef, BigDecimal amount, String idempotentKey) {

    final var transactionToCreate =
        TransactionHelper.buildTopUpTransaction(userRef.getId(), null, amount, idempotentKey);
    final var createdTransaction = transactionRepository.save(transactionToCreate);
    logger.info(
        "[createTopUpTransaction] New {} is created: {}",
        Transaction.class.getSimpleName(),
        createdTransaction);

    return createdTransaction;
  }

  private TourCompanyWallet getTourCompanyWalletByTourCompanyId(int tourCompanyId) {
    logger.debug(
        "[getTourCompanyWalletByTourCompanyId] Getting {} by tourCompanyId [{}]",
        TourCompanyWallet.class.getSimpleName(),
        tourCompanyId);

    return tourCompanyWalletRepository
        .findOneByTourCompanyId(AggregateReference.to(tourCompanyId))
        .orElseThrow(
            errorHelper.entityWithSubResourceNotFound(
                "getTourCompanyWalletByTourCompanyId",
                TourCompanyWallet.class,
                "tourCompanyId",
                String.valueOf(tourCompanyId)));
  }

  private Integer getIdOrNull(AggregateReference<?, Integer> resourceRef) {
    return resourceRef != null ? resourceRef.getId() : null;
  }
}
