package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.Transaction;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.TransactionRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public WalletServiceImpl(
      UserWalletRepository userWalletRepository,
      TourCompanyWalletRepository tourCompanyWalletRepository,
      TransactionRepository transactionRepository) {

    this.userWalletRepository = userWalletRepository;
    this.tourCompanyWalletRepository = tourCompanyWalletRepository;
    this.transactionRepository = transactionRepository;
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

    return UserWalletInfoResponse.fromDao(findUserWalletByUserId(userId));
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
    var walletToDelete = findUserWalletByUserId(userId);

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

  private UserWallet findUserWalletByUserId(int userId) {
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

    return Transaction.of(
        null, userRef, null, Instant.now(), amount, TransactionType.TOP_UP, idempotentKey);
  }
}
