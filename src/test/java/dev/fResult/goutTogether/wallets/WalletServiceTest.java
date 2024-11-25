package dev.fResult.goutTogether.wallets;

import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import dev.fResult.goutTogether.wallets.services.WalletServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
  @InjectMocks private WalletServiceImpl walletService;

  @Mock UserWalletRepository userWalletRepository;
  @Mock TourCompanyWalletRepository tourCompanyWalletRepository;
}
