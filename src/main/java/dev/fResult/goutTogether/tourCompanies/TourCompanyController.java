package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyResponse;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<List<TourCompanyResponse>> get() {
    logger.info("Getting all tour companies");
    return ResponseEntity.ok(tourCompanyService.getTourCompanies());
  }

  @GetMapping("/{id}")
  public ResponseEntity<TourCompanyResponse> get(@PathVariable int id) {
    logger.info("Getting {} id [{}]", TourCompany.class.getSimpleName(), id);

    return ResponseEntity.ok(tourCompanyService.getTourCompanyById(id));
  }

  @PostMapping
  public ResponseEntity<TourCompanyResponse> register(
      @RequestBody @Validated TourCompanyRegistrationRequest body) {
    logger.info("Registering a new tour company");
    return ResponseEntity.created(URI.create("/api/v1/tour-companies"))
        .body(tourCompanyService.registerTourCompany(body));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<TourCompanyResponse> approve(@PathVariable int id) {
    logger.info("Approving a new tour company id [{}]", id);
    var approvedCompany = tourCompanyService.approveTourCompany(id);
    logger.info("[approvedCompany] company id [{}] is approved", id);
    return ResponseEntity.ok(approvedCompany);
  }
}
