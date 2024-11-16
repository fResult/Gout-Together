package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.dtos.RegisterTourCompanyRequest;
import dev.fResult.goutTogether.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompanyLogin;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourCompanyServiceImpl implements TourCompanyService {
  private final Logger logger = LoggerFactory.getLogger(TourCompanyServiceImpl.class);

  private final TourCompanyRepository tourCompanyRepository;
  private final TourCompanyLoginRepository tourCompanyLoginRepository;
  private final PasswordEncoder passwordEncoder;

  public TourCompanyServiceImpl(
      TourCompanyRepository tourCompanyRepository,
      TourCompanyLoginRepository tourCompanyLoginRepository,
      PasswordEncoder passwordEncoder) {
    this.tourCompanyRepository = tourCompanyRepository;
    this.tourCompanyLoginRepository = tourCompanyLoginRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public TourCompany registerTourCompany(RegisterTourCompanyRequest body) {
    logger.debug("[registerTourCompany] newly tour company is registering");
    var companyToRegister = TourCompany.of(null, body.name(), TourCompanyStatus.WAITING.name());
    var registeredCompany = tourCompanyRepository.save(companyToRegister);
    logger.info("[registerTourCompany] new tour company: {} is registered", registeredCompany);

    var companyCredential = buildTourCompanyLogin(registeredCompany, body);
    tourCompanyLoginRepository.save(companyCredential);
    logger.info(
        "[registerTourCompany] new tour company credential: {} is created", companyCredential);

    return registeredCompany;
  }

  @Override
  public TourCompany approveTourCompany(int id) {
    logger.debug("[approveTourCompany] tour company id [{}] is approving", id);
    return tourCompanyRepository
        .findById(id)
        .map(
            existingCompany -> {
              if (existingCompany.status().equals(TourCompanyStatus.APPROVED.name())) {
                logger.warn(
                    "[approveTourCompany] tour company with id [{}] is already approved", id);
                try {
                  throw new Exception(
                      String.format(
                          "[approveTourCompany] Tour company id [%s] is already approved", id));
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              }
              var companyToApprove =
                  TourCompany.of(
                      existingCompany.id(),
                      existingCompany.name(),
                      TourCompanyStatus.APPROVED.name());
              var approvedCompany = tourCompanyRepository.save(companyToApprove);
              logger.info("[approveTour] approved tour company: {}", approvedCompany);

              // TODO: Create wallet for approved company
              return approvedCompany;
            })
        .orElseThrow(
            () -> {
              logger.warn("[approveTour] tour company id [{}] not found", id);
              return new EntityNotFound(String.format("Tour company id [%s] not found", id));
            });
  }

  private TourCompanyLogin buildTourCompanyLogin(
      TourCompany company, RegisterTourCompanyRequest body) {
    var encryptedPassword = passwordEncoder.encode(body.password());

    return TourCompanyLogin.of(
        null, AggregateReference.to(company.id()), body.username(), encryptedPassword);
  }
}
