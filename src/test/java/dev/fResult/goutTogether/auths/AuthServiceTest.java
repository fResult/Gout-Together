package dev.fResult.goutTogether.auths;

import dev.fResult.goutTogether.auths.services.AuthServiceImpl;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @InjectMocks private AuthServiceImpl authService;

  @Mock private UserLoginRepository userLoginRepository;
  @Mock private TourCompanyLoginRepository tourCompanyLoginRepository;
  @Mock private PasswordEncoder passwordEncoder;
}
