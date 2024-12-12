package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.TransactionRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
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
  public UserWallet findConsumerWalletByUserId(int userId) {
    logger.debug(
        "[findUserWalletByUserId] Finding {} by userId: {}",
        UserWallet.class.getSimpleName(),
        userId);

    return userWalletRepository
        .findOneByUserId(AggregateReference.to(userId))
        .orElseThrow(
            errorHelper.entityNotFound("findUserWalletByUserId", UserWallet.class, userId));
  }

  @Override
  @Transactional
  public UserWalletInfoResponse topUpConsumerWallet(
      int userId, UUID idempotentKey, WalletTopUpRequest body) {
    var existsTransactionOpt = transactionRepository.findOneByIdempotentKey(idempotentKey);

    var userWallet =
        userWalletRepository
            .findOneByUserId(AggregateReference.to(userId))
            .map(UserWalletInfoResponse::fromDao)
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "topUpConsumerWallet", UserWallet.class, "userId", String.valueOf(userId)));

    if (existsTransactionOpt.isPresent()) return userWallet;

    var balanceToUpdate = userWallet.balance().add(body.amount());
    var userWalletToUpdate =
        UserWallet.of(
            userWallet.id(),
            AggregateReference.to(userWallet.userId()),
            Instant.now(),
            balanceToUpdate);

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
    var walletToDelete = findConsumerWalletByUserId(userId);

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
}
