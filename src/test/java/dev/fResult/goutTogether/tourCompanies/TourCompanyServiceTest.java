package dev.fResult.goutTogether.tourCompanies;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.common.exceptions.ValidationException;
import dev.fResult.goutTogether.tourCompanies.dtos.RegisterTourCompanyRequest;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompanyLogin;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompanyWallet;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyRepository;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TourCompanyServiceTest {
  @InjectMocks private TourCompanyServiceImpl tourCompanyService;

  @Mock private TourCompanyRepository tourCompanyRepository;
  @Mock private TourCompanyLoginRepository tourCompanyLoginRepository;
  @Mock private TourCompanyWalletRepository tourCompanyWalletRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @Test
  void whenRegisterCompanyThenSuccess() {
    // Arrange
    var body = RegisterTourCompanyRequest.of(null, "My Tour", "MyTour", "mypassword", null);
    var expectedRegisteredCompany = TourCompany.of(1, "My Tour", TourCompanyStatus.WAITING.name());

    var mockTourCompany = TourCompany.of(1, "My Tour", TourCompanyStatus.WAITING.name());
    var mockCompanyCredential =
        TourCompanyLogin.of(
            null,
            AggregateReference.to(mockTourCompany.id()),
            body.username(),
            "encryptedPassword");
    when(tourCompanyRepository.save(any(TourCompany.class))).thenReturn(mockTourCompany);
    when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encryptedPassword");
    when(tourCompanyLoginRepository.save(any(TourCompanyLogin.class)))
        .thenReturn(mockCompanyCredential);

    // Actual
    var actualRegisteredCompany = tourCompanyService.registerTourCompany(body);

    // Assert
    assertNotNull(actualRegisteredCompany);
    assertEquals(expectedRegisteredCompany, actualRegisteredCompany);
  }

  @Test
  void whenApproveTourCompanyThenSuccess() {
    // Arrange
    var mockTourCompany = TourCompany.of(1, "My Tour", TourCompanyStatus.WAITING.name());
    var mockApprovedTourCompany =
        TourCompany.of(
            mockTourCompany.id(), mockTourCompany.name(), TourCompanyStatus.APPROVED.name());
    var mockCreatedCompanyWallet =
        TourCompanyWallet.of(
            null,
            AggregateReference.to(mockApprovedTourCompany.id()),
            Instant.now(),
            BigDecimal.ZERO);

    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.of(mockTourCompany));
    when(tourCompanyRepository.save(any(TourCompany.class))).thenReturn(mockApprovedTourCompany);
    when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
        .thenReturn(mockCreatedCompanyWallet);

    // Actual
    var actualApprovedCompany = tourCompanyService.approveTourCompany(1);

    // Assert
    assertNotNull(actualApprovedCompany);
    assertEquals(mockApprovedTourCompany, actualApprovedCompany);
    assertNotNull(mockCreatedCompanyWallet);
  }

  }

  @Test
  void whenApproveCompanyButTourCompanyNotFoundThenError() {
    // Arrange
    var TOUR_ID = 99999;
    var expectedErrorMessage = String.format("Tour company id [%d] not found", TOUR_ID);
    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> tourCompanyService.approveTourCompany(TOUR_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFound.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
