package dev.fResult.goutTogether.tourCompanies.services;

import java.math.BigDecimal;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class TourCompanyServiceImpl implements TourCompanyService {
  private final Logger logger = LoggerFactory.getLogger(TourCompanyServiceImpl.class);

  private final TourCompanyRepository tourCompanyRepository;
  private final TourCompanyLoginRepository tourCompanyLoginRepository;
  private final TourCompanyWalletRepository tourCompanyWalletRepository;
  private final PasswordEncoder passwordEncoder;

  public TourCompanyServiceImpl(
      TourCompanyRepository tourCompanyRepository,
      TourCompanyLoginRepository tourCompanyLoginRepository,
      TourCompanyWalletRepository tourCompanyWalletRepository,
      PasswordEncoder passwordEncoder) {
    this.tourCompanyRepository = tourCompanyRepository;
    this.tourCompanyLoginRepository = tourCompanyLoginRepository;
    this.tourCompanyWalletRepository = tourCompanyWalletRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public TourCompany registerTourCompany(RegisterTourCompanyRequest body) {
    logger.debug("[registerTourCompany] newly tour company is registering");
    var companyToRegister = TourCompany.of(null, body.name(), TourCompanyStatus.WAITING.name());
    var registeredCompany = tourCompanyRepository.save(companyToRegister);
    logger.info("[registerTourCompany] new tour company: {} is registered", registeredCompany);

    createTourCompanyLogin(registeredCompany, body);

    return registeredCompany;
  }

  @Override
  @Transactional
  public TourCompany approveTourCompany(int id) {
    logger.debug("[approveTourCompany] tour company id [{}] is approving", id);
    var tourCompany =
        tourCompanyRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  logger.warn("[approveTourCompany] tour company id [{}] not found", id);
                  return new EntityNotFound(String.format("Tour company id [%s] not found", id));
                });

    if (tourCompany.status().equals(TourCompanyStatus.APPROVED.name())) {
      logger.warn("[approveTourCompany] tour company with id [{}] is already approved", id);
      throw new ValidationException(String.format("Tour company id [%s] is already approved", id));
    }

    var companyToApprove =
        TourCompany.of(tourCompany.id(), tourCompany.name(), TourCompanyStatus.APPROVED.name());
    var approvedCompany = tourCompanyRepository.save(companyToApprove);
    logger.info("[approveTourCompany] approved tour company: {}", approvedCompany);
    createCompanyWallet(approvedCompany);

    return approvedCompany;
  }

  @Override
  public TourCompany getTourCompanyById(int id) {
    return tourCompanyRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFound(String.format("Tour company id [%s] not found", id)));
  }

  private void createTourCompanyLogin(TourCompany company, RegisterTourCompanyRequest body) {
    var encryptedPassword = passwordEncoder.encode(body.password());

    var companyCredentialToCreate =
        TourCompanyLogin.of(
            null, AggregateReference.to(company.id()), body.username(), encryptedPassword);

    tourCompanyLoginRepository.save(companyCredentialToCreate);
    logger.info(
        "[registerTourCompany] new tour company credential: {} is created",
        companyCredentialToCreate);
  }

  private void createCompanyWallet(TourCompany company) {
    var companyWalletToCreate =
        TourCompanyWallet.of(
            null, AggregateReference.to(company.id()), Instant.now(), BigDecimal.ZERO);

    tourCompanyWalletRepository.save(companyWalletToCreate);
    logger.info(
        "[approveTourCompany] new tour company wallet: {} is created", companyWalletToCreate);
  }
}
