package dev.fResult.goutTogether.tourCompanies.services;

import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.common.exceptions.ValidationException;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyRepository;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourCompanyServiceImpl implements TourCompanyService {
  private final Logger logger = LoggerFactory.getLogger(TourCompanyServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(TourCompanyServiceImpl.class);

  private final TourCompanyRepository tourCompanyRepository;
  private final TourCompanyLoginRepository tourCompanyLoginRepository;
  private final TourCompanyWalletRepository tourCompanyWalletRepository;
  private final AuthService authService;
  private final PasswordEncoder passwordEncoder;

  public TourCompanyServiceImpl(
      TourCompanyRepository tourCompanyRepository,
      TourCompanyLoginRepository tourCompanyLoginRepository,
      TourCompanyWalletRepository tourCompanyWalletRepository,
      AuthService authService,
      PasswordEncoder passwordEncoder) {
    this.tourCompanyRepository = tourCompanyRepository;
    this.tourCompanyLoginRepository = tourCompanyLoginRepository;
    this.tourCompanyWalletRepository = tourCompanyWalletRepository;
    this.authService = authService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public TourCompany registerTourCompany(TourCompanyRegistrationRequest body) {
    logger.debug("[registerTourCompany] New {} is registering", TourCompany.class.getSimpleName());

    var existingCompanyCredential =
        authService.findTourCompanyCredentialByUsername(body.username());
    if (existingCompanyCredential.isPresent()) {
      logger.warn(
          "[registerTourCompany] {} username [{}] already exists",
          TourCompanyLogin.class.getSimpleName(),
          body.username());
      throw new CredentialExistsException(
          String.format(
              "%s username [%s] already exists",
              TourCompanyLogin.class.getSimpleName(), body.username()));
    }

    var companyToRegister = TourCompany.of(null, body.name(), TourCompanyStatus.WAITING.name());
    var registeredCompany = tourCompanyRepository.save(companyToRegister);
    logger.info(
        "[registerTourCompany] New {} is registered: {}",
        TourCompany.class.getSimpleName(),
        registeredCompany);

    authService.createTourCompanyLogin(registeredCompany.id(), body.username(), body.password());

    return registeredCompany;
  }

  @Override
  @Transactional
  public TourCompany approveTourCompany(int id) {
    logger.debug(
        "[approveTourCompany] {} id [{}] is approving", TourCompany.class.getSimpleName(), id);
    var tourCompany = getTourCompanyById(id);

    if (tourCompany.status().equals(TourCompanyStatus.APPROVED.name())) {
      logger.warn(
          "[approveTourCompany] {} id [{}] is already approved",
          TourCompany.class.getSimpleName(),
          id);
      throw new ValidationException(String.format("Tour company id [%s] is already approved", id));
    }

    var companyToApprove =
        TourCompany.of(tourCompany.id(), tourCompany.name(), TourCompanyStatus.APPROVED.name());
    var approvedCompany = tourCompanyRepository.save(companyToApprove);
    logger.info(
        "[approveTourCompany] approved {}: {}", TourCompany.class.getSimpleName(), approvedCompany);
    createCompanyWallet(approvedCompany);

    return approvedCompany;
  }

  @Override
  public TourCompany getTourCompanyById(int id) {
    return tourCompanyRepository
        .findById(id)
        .orElseThrow(errorHelper.entityNotFound("getTourCompanyById", TourCompany.class, id));
  }

  private void createCompanyWallet(TourCompany company) {
    var companyWalletToCreate =
        TourCompanyWallet.of(
            null, AggregateReference.to(company.id()), Instant.now(), BigDecimal.ZERO);

    tourCompanyWalletRepository.save(companyWalletToCreate);
    logger.info(
        "[approveTourCompany] New {} is created: {}",
        TourCompanyWallet.class.getSimpleName(),
        companyWalletToCreate);
  }
}
