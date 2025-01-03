package dev.fResult.goutTogether.tourCompanies;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.exceptions.ValidationException;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyResponse;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyUpdateRequest;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyRepository;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyServiceImpl;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class TourCompanyServiceTest {
  private static final int TOUR_COMPANY_ID = 1;
  private static final int NOT_FOUND_TOUR_COMPANY_ID = 99999;
  private static final String TARGET_TOUR_COMPANY_USERNAME = "target_company_username";

  @InjectMocks private TourCompanyServiceImpl tourCompanyService;

  @Mock private TourCompanyRepository tourCompanyRepository;
  @Mock private AuthService authService;
  @Mock private WalletService walletService;

  @Test
  void whenGetTourCompanies_ThenSuccess() {
    // Arrange
    var mockTourCompany1 = TourCompany.of(1, "My Tour 1", TourCompanyStatus.WAITING.name());
    var mockTourCompany2 = TourCompany.of(2, "My Tour 2", TourCompanyStatus.APPROVED.name());
    var expectedTourCompanies =
        List.of(
            TourCompanyResponse.fromDao(mockTourCompany1),
            TourCompanyResponse.fromDao(mockTourCompany2));
    when(tourCompanyRepository.findAll()).thenReturn(List.of(mockTourCompany1, mockTourCompany2));

    // Actual
    var actualTourCompanies = tourCompanyService.getTourCompanies();

    // Assert
    assertNotNull(actualTourCompanies);
    assertEquals(expectedTourCompanies, actualTourCompanies);
  }

  @Test
  void whenRegisterCompany_ThenSuccess() {
    // Arrange
    var body = TourCompanyRegistrationRequest.of("My Tour", "MyTour", "mypassword", null);

    var mockTourCompany = TourCompany.of(1, "My Tour", TourCompanyStatus.WAITING.name());
    var expectedRegisteredCompany =
        TourCompanyResponse.of(
            mockTourCompany.id(),
            mockTourCompany.name(),
            TourCompanyStatus.valueOf(mockTourCompany.status()));
    var mockCompanyCredential =
        TourCompanyLogin.of(
            null,
            AggregateReference.to(mockTourCompany.id()),
            body.username(),
            "encryptedPassword");
    when(authService.findTourCompanyCredentialByUsername(anyString())).thenReturn(Optional.empty());
    when(tourCompanyRepository.save(any(TourCompany.class))).thenReturn(mockTourCompany);
    when(authService.createTourCompanyLogin(anyInt(), anyString(), anyString()))
        .thenReturn(mockCompanyCredential);

    // Actual
    var actualRegisteredCompany = tourCompanyService.registerTourCompany(body);

    // Assert
    assertNotNull(actualRegisteredCompany);
    assertEquals(expectedRegisteredCompany, actualRegisteredCompany);
  }

  @Test
  void whenRegisterCompany_ButUsernameAlreadyExists_ThenThrowCredentialExistsException() {
    // Arrange
    var expectedErrorMessage =
        String.format(
            "%s username [%s] already exists",
            TourCompanyLogin.class.getSimpleName(), TARGET_TOUR_COMPANY_USERNAME);
    var body =
        TourCompanyRegistrationRequest.of(
            "My Tour", TARGET_TOUR_COMPANY_USERNAME, "mypassword", null);

    var mockTourCompany = TourCompany.of(1, "My Tour", TourCompanyStatus.WAITING.name());
    var mockCompanyCredential =
        TourCompanyLogin.of(
            null,
            AggregateReference.to(mockTourCompany.id()),
            body.username(),
            "encryptedPassword");
    when(authService.findTourCompanyCredentialByUsername(anyString()))
        .thenReturn(Optional.of(mockCompanyCredential));

    // Actual
    Executable actualExecutable = () -> tourCompanyService.registerTourCompany(body);

    // Assert
    var exception = assertThrowsExactly(CredentialExistsException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenApproveTourCompany_ThenSuccess() {
    // Arrange
    var mockTourCompany = TourCompany.of(1, "My Tour", TourCompanyStatus.WAITING.name());
    var mockApprovedTourCompany =
        TourCompany.of(
            mockTourCompany.id(), mockTourCompany.name(), TourCompanyStatus.APPROVED.name());
    var expectedApprovedCompany =
        TourCompanyResponse.of(
            mockApprovedTourCompany.id(),
            mockApprovedTourCompany.name(),
            TourCompanyStatus.valueOf(mockApprovedTourCompany.status()));
    var mockCreatedCompanyWallet =
        TourCompanyWallet.of(
            null,
            AggregateReference.to(mockApprovedTourCompany.id()),
            Instant.now(),
            BigDecimal.ZERO);

    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.of(mockTourCompany));
    when(tourCompanyRepository.save(any(TourCompany.class))).thenReturn(mockApprovedTourCompany);
    when(walletService.createTourCompanyWallet(anyInt())).thenReturn(mockCreatedCompanyWallet);

    // Actual
    var actualApprovedCompany = tourCompanyService.approveTourCompany(1);

    // Assert
    assertNotNull(actualApprovedCompany);
    assertEquals(expectedApprovedCompany, actualApprovedCompany);
    assertNotNull(mockCreatedCompanyWallet);
  }

  @Test
  void whenApproveCompany_ButCompanyIsAlreadyApproved_ThenThrowValidationException() {
    // Arrange
    var expectedErrorMessage =
        String.format("Tour company id [%d] is already approved", TOUR_COMPANY_ID);
    when(tourCompanyRepository.findById(anyInt()))
        .thenReturn(
            Optional.of(
                TourCompany.of(TOUR_COMPANY_ID, "My Tour", TourCompanyStatus.APPROVED.name())));

    // Actual
    Executable actualExecutable = () -> tourCompanyService.approveTourCompany(TOUR_COMPANY_ID);

    // Assert
    var exception = assertThrows(ValidationException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenApproveCompany_ButCompanyNotFoundThrowEntityNotFoundException() {
    // Arrange
    var expectedErrorMessage =
        String.format(
            "%s id [%d] not found", TourCompany.class.getSimpleName(), NOT_FOUND_TOUR_COMPANY_ID);
    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable =
        () -> tourCompanyService.approveTourCompany(NOT_FOUND_TOUR_COMPANY_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenGetCompanyById_ThenSuccess() {
    // Arrange
    var mockCompany = TourCompany.of(TOUR_COMPANY_ID, "My Tour", TourCompanyStatus.WAITING.name());
    var expectedCompanyResponse =
        TourCompanyResponse.of(
            mockCompany.id(), mockCompany.name(), TourCompanyStatus.valueOf(mockCompany.status()));
    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.of(mockCompany));

    // Actual
    var actualCompany = tourCompanyService.getTourCompanyById(TOUR_COMPANY_ID);

    // Assert
    assertEquals(expectedCompanyResponse, actualCompany);
  }

  @Test
  void whenGetCompanyById_ButCompanyNotFound_ThenThrowEntityNotFoundException() {
    // Arrange
    var expectedErrorMessage =
        String.format("%s id [%d] not found", TourCompany.class.getSimpleName(), TOUR_COMPANY_ID);
    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> tourCompanyService.getTourCompanyById(TOUR_COMPANY_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenUpdateCompanyById_ThenSuccess() {
    // Arrange
    var TOUR_NAME_TO_UPDATE = "Your Tour";
    var body = TourCompanyUpdateRequest.of(TOUR_NAME_TO_UPDATE, null);
    var mockExistingTourCompany =
        TourCompany.of(TOUR_COMPANY_ID, "My Tour", TourCompanyStatus.APPROVED.name());
    var mockUpdatedTourCompany =
        TourCompany.of(TOUR_COMPANY_ID, TOUR_NAME_TO_UPDATE, mockExistingTourCompany.status());
    var expectedUpdatedCompany =
        TourCompanyResponse.of(
            TOUR_COMPANY_ID,
            TOUR_NAME_TO_UPDATE,
            TourCompanyStatus.valueOf(mockExistingTourCompany.status()));

    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.of(mockExistingTourCompany));
    when(tourCompanyRepository.save(any(TourCompany.class))).thenReturn(mockUpdatedTourCompany);

    // Actual
    var actualUpdatedCompany = tourCompanyService.updateTourCompanyById(TOUR_COMPANY_ID, body);

    // Assert
    assertEquals(expectedUpdatedCompany, actualUpdatedCompany);
  }

  @Test
  void whenUpdateCompanyById_ButNotFound_ThenThrowException() {
    // Arrange
    var expectedErrorMessage =
        String.format(
            "%s id [%d] not found", TourCompany.class.getSimpleName(), NOT_FOUND_TOUR_COMPANY_ID);
    var body = TourCompanyUpdateRequest.of("Your Tour", null);
    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable =
        () -> tourCompanyService.updateTourCompanyById(NOT_FOUND_TOUR_COMPANY_ID, body);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenDeleteCompanyById_ThenSuccess() {
    // Arrange
    var mockTourCompany =
        TourCompany.of(TOUR_COMPANY_ID, "My Tour", TourCompanyStatus.APPROVED.name());
    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.of(mockTourCompany));
    doNothing().when(tourCompanyRepository).deleteById(anyInt());

    // Actual
    tourCompanyService.deleteTourCompanyById(TOUR_COMPANY_ID);

    // Assert
    verify(tourCompanyRepository, times(1)).deleteById(TOUR_COMPANY_ID);
  }

  @Test
  void whenDeleteCompanyById_ButTourCompanyNotFound_ThenThrowNotFoundException() {
    // Arrange
    var expectedErrorMessage =
        String.format(
            "%s id [%d] not found", TourCompany.class.getSimpleName(), NOT_FOUND_TOUR_COMPANY_ID);
    when(tourCompanyRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable =
        () -> tourCompanyService.deleteTourCompanyById(NOT_FOUND_TOUR_COMPANY_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
