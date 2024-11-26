package dev.fResult.goutTogether.users;

import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.users.repositories.UserRepository;
import dev.fResult.goutTogether.users.services.UserServiceImpl;
import dev.fResult.goutTogether.wallets.services.WalletService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @InjectMocks private UserServiceImpl userService;

  @Mock UserRepository userRepository;
  @Mock AuthService authService;
  @Mock WalletService walletService;
}
