package dev.fResult.goutTogether.tourCompanies.services;

import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.common.exceptions.ValidationException;
import dev.fResult.goutTogether.common.utils.StringUtil;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyResponse;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyUpdateRequest;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyRepository;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourCompanyServiceImpl implements TourCompanyService {
  private final Logger logger = LoggerFactory.getLogger(TourCompanyServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(TourCompanyServiceImpl.class);

  private final TourCompanyRepository tourCompanyRepository;
  private final AuthService authService;
  private final WalletService walletService;

  public TourCompanyServiceImpl(
      TourCompanyRepository tourCompanyRepository,
      AuthService authService,
      WalletService walletService) {
    this.tourCompanyRepository = tourCompanyRepository;
    this.authService = authService;
    this.walletService = walletService;
  }

  @Override
  public List<TourCompanyResponse> getTourCompanies() {
    logger.debug(
        "[getTourCompanies] Getting all {}",
        StringUtil.pluralize(TourCompany.class.getSimpleName()));

    return tourCompanyRepository.findAll().stream().map(TourCompanyResponse::fromDao).toList();
  }

  @Override
  public TourCompanyResponse getTourCompanyById(int id) {
    return getOptTourCompanyById(id)
        .orElseThrow(errorHelper.entityNotFound("getTourCompanyById", TourCompany.class, id));
  }

  @Override
  @Transactional
  public TourCompanyResponse registerTourCompany(TourCompanyRegistrationRequest body) {
    logger.debug("[registerTourCompany] New {} is registering", TourCompany.class.getSimpleName());
    throwExceptionIfTourCompanyUserNameAlreadyExists(body.username());

    var companyToRegister = TourCompany.of(null, body.name(), TourCompanyStatus.WAITING.name());
    var registeredCompany = tourCompanyRepository.save(companyToRegister);
    logger.info(
        "[registerTourCompany] New {} is registered: {}",
        TourCompany.class.getSimpleName(),
        registeredCompany);

    authService.createTourCompanyLogin(registeredCompany.id(), body.username(), body.password());

    return TourCompanyResponse.fromDao(registeredCompany);
  }

  @Override
  @Transactional
  public TourCompanyResponse approveTourCompany(int id) {
    logger.debug(
        "[approveTourCompany] {} id [{}] is approving", TourCompany.class.getSimpleName(), id);
    var tourCompany = getTourCompanyById(id);

    if (tourCompany.status().equals(TourCompanyStatus.APPROVED)) {
      logger.warn(
          "[approveTourCompany] {} id [{}] is already approved",
          TourCompany.class.getSimpleName(),
          tourCompany.id());
      throw new ValidationException(String.format("Tour company id [%s] is already approved", id));
    }

    var companyToApprove =
        TourCompany.of(tourCompany.id(), tourCompany.name(), TourCompanyStatus.APPROVED.name());
    var approvedCompany = tourCompanyRepository.save(companyToApprove);
    logger.info(
        "[approveTourCompany] {} is approved: {}",
        TourCompany.class.getSimpleName(),
        approvedCompany);

    walletService.createTourCompanyWallet(approvedCompany.id());

    return TourCompanyResponse.fromDao(approvedCompany);
  }

  @Override
  public TourCompanyResponse updateTourCompanyById(int id, TourCompanyUpdateRequest body) {
    logger.debug(
        "[updateTourCompanyById] {} id [{}] is updating", TourCompany.class.getSimpleName(), id);
    var toTourCompanyUpdate = TourCompanyUpdateRequest.dtoToTourCompanyUpdate(body);
    var companyToUpdate =
        tourCompanyRepository
            .findById(id)
            .map(toTourCompanyUpdate)
            .orElseThrow(
                errorHelper.entityNotFound("updateTourCompanyById", TourCompany.class, id));

    var updatedCompany = tourCompanyRepository.save(companyToUpdate);
    logger.info(
        "[updateTourCompanyById] {} id [{}] is updated",
        TourCompany.class.getSimpleName(),
        updatedCompany.id());

    return TourCompanyResponse.fromDao(updatedCompany);
  }

  @Override
  public boolean deleteTourCompanyById(int id) {
    logger.debug(
        "[deleteTourCompanyById] {} id [{}] is deleting", TourCompany.class.getSimpleName(), id);
    var existingCompany =
        getOptTourCompanyById(id)
            .orElseThrow(
                errorHelper.entityNotFound("deleteTourCompanyById", TourCompany.class, id));

    tourCompanyRepository.deleteById(existingCompany.id());
    logger.info(
        "[deleteTourCompanyById] {} id [{}] is deleted",
        TourCompany.class.getSimpleName(),
        existingCompany.id());

    return true;
  }

  private Optional<TourCompanyResponse> getOptTourCompanyById(int id) {
    return tourCompanyRepository
        .findById(id)
        .flatMap(company -> Optional.of(TourCompanyResponse.fromDao(company)));
  }

  private void throwExceptionIfTourCompanyUserNameAlreadyExists(String username) {
    var existingCompanyCredential = authService.findTourCompanyCredentialByUsername(username);
    if (existingCompanyCredential.isPresent()) {
      logger.warn(
          "[registerTourCompany] {} username [{}] already exists",
          TourCompanyLogin.class.getSimpleName(),
          username);
      throw new CredentialExistsException(
          String.format(
              "%s username [%s] already exists", TourCompanyLogin.class.getSimpleName(), username));
    }
  }
}
