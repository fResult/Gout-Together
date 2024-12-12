package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {
  private final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(WalletServiceImpl.class);

  private final UserWalletRepository userWalletRepository;
  private final TourCompanyWalletRepository tourCompanyWalletRepository;

  public WalletServiceImpl(
      UserWalletRepository userWalletRepository,
      TourCompanyWalletRepository tourCompanyWalletRepository) {
    this.userWalletRepository = userWalletRepository;
    this.tourCompanyWalletRepository = tourCompanyWalletRepository;
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
  }

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
