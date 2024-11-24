package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {
  private final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

  private final UserWalletRepository userWalletRepository;

  public WalletServiceImpl(UserWalletRepository userWalletRepository) {
    this.userWalletRepository = userWalletRepository;
  }

  @Override
  public UserWallet createConsumerWallet(int userId) {
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
  public Optional<UserWallet> findUserWalletByUserId(int userId) {
    return userWalletRepository.findById(userId);
  }
}
