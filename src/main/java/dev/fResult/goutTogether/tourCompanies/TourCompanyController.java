package dev.fResult.goutTogether.tourCompanies;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;

import dev.fResult.goutTogether.common.utils.StringUtil;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyResponse;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyUpdateRequest;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tour-companies")
public class TourCompanyController {
  private final Logger logger = LoggerFactory.getLogger(TourCompanyController.class);

  private final TourCompanyService tourCompanyService;

  public TourCompanyController(TourCompanyService tourCompanyService) {
    this.tourCompanyService = tourCompanyService;
  }

  @GetMapping
  public ResponseEntity<List<TourCompanyResponse>> getTourCompanies() {
    logger.debug(
        "[getTourCompanies] Getting all {}",
        StringUtil.pluralize(TourCompany.class.getSimpleName()));

    return ResponseEntity.ok(tourCompanyService.getTourCompanies());
  }

  @GetMapping("/{id}")
  public ResponseEntity<TourCompanyResponse> getTourCompanyById(@PathVariable int id) {
    logger.debug("[getTourCompanyById] Getting {} id [{}]", TourCompany.class.getSimpleName(), id);

    return ResponseEntity.ok(tourCompanyService.getTourCompanyById(id));
  }

  @GetMapping("/me")
  public ResponseEntity<TourCompanyResponse> getMyTourCompany(Authentication authentication) {
    final var jwt = (Jwt) authentication.getPrincipal();
    final var tourCompanyId = jwt.getClaimAsString(RESOURCE_ID_CLAIM);

    logger.debug(
        "[getMyTourCompany] Getting {} auth of id [{}]",
        TourCompany.class.getSimpleName(),
        tourCompanyId);

    return ResponseEntity.ok(
        tourCompanyService.getTourCompanyById(Integer.parseInt(tourCompanyId)));
  }

  @PostMapping
  public ResponseEntity<TourCompanyResponse> registerTourCompany(
      @RequestBody @Validated TourCompanyRegistrationRequest body) {

    logger.info("[registerTourCompany] Registering a new {}", TourCompany.class.getSimpleName());

    return ResponseEntity.created(URI.create("/api/v1/tour-companies"))
        .body(tourCompanyService.registerTourCompany(body));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<TourCompanyResponse> approveTourCompanyById(@PathVariable int id) {
    logger.debug(
        "[approveTourCompanyById] Approving a new {} id [{}]",
        TourCompany.class.getSimpleName(),
        id);
    final var approvedCompany = tourCompanyService.approveTourCompany(id);

    return ResponseEntity.ok(approvedCompany);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<TourCompanyResponse> updateTourCompanyById(
      @PathVariable int id, @RequestBody @Validated TourCompanyUpdateRequest body) {

    logger.debug(
        "[updateTourCompanyById] Updating a {} id [{}]", TourCompany.class.getSimpleName(), id);

    return ResponseEntity.ok(tourCompanyService.updateTourCompanyById(id, body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteTourCompanyById(@PathVariable int id) {
    logger.debug(
        "[deleteTourCompanyById] Deleting a {} id [{}]", TourCompany.class.getSimpleName(), id);
    tourCompanyService.deleteTourCompanyById(id);

    return ResponseEntity.ok(
        String.format("Delete %s by id [%d] successfully", TourCompany.class.getSimpleName(), id));
  }
}
