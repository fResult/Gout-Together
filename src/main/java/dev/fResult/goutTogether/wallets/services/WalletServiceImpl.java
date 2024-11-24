package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.users.entities.User;
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

    AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
    Instant currentTime = Instant.now();
    BigDecimal initialBalance = BigDecimal.ZERO;
    var createdWallet = UserWallet.of(null, userReference, currentTime, initialBalance);
    userWalletRepository.save(createdWallet);

    logger.info(
        "[createConsumerWallet] New {} is created: {}",
        UserWallet.class.getSimpleName(),
        createdWallet);

    return createdWallet;
  }

  @Override
  public UserWallet findUserWalletByUserId(int userId) {
    logger.debug(
        "[findUserWalletByUserId] Finding {} by userId: {}",
        UserWallet.class.getSimpleName(),
        userId);

    return userWalletRepository
        .findById(userId)
        .orElseThrow(
            errorHelper.entityNotFound("findUserWalletByUserId", UserWallet.class, userId));
  }

  @Override
  public TourCompanyWallet createTourCompanyWallet(int tourCompanyId) {
    logger.debug(
        "[createTourCompanyWallet] New {} is creating", TourCompanyWallet.class.getSimpleName());

    AggregateReference<TourCompany, Integer> tourCompanyReference =
        AggregateReference.to(tourCompanyId);
    var createdWallet =
        TourCompanyWallet.of(null, tourCompanyReference, Instant.now(), BigDecimal.ZERO);

    tourCompanyWalletRepository.save(createdWallet);
    logger.info(
        "[createTourCompanyWallet] New {} is created: {}",
        TourCompanyWallet.class.getSimpleName(),
        createdWallet);

    return createdWallet;
  }

  public boolean deleteUserWalletById(int userId) {
    logger.debug(
        "[deleteUserWalletById] Deleting {} by id: {}", UserWallet.class.getSimpleName(), userId);
    var walletToDelete = findUserWalletByUserId(userId);

    userWalletRepository.delete(walletToDelete);
    logger.info(
        "[deleteUserWalletById] {} id [{}] is deleted", UserWallet.class.getSimpleName(), userId);

    return true;
  }
}
