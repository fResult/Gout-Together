package dev.fResult.goutTogether.tours;

import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import dev.fResult.goutTogether.tours.dtos.TourRequest;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourService;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tours")
public class TourController {
  private final Logger logger = LoggerFactory.getLogger(TourController.class);

  private final TourService tourService;

  public TourController(TourService tourService, TourCompanyService tourCompanyService) {
    this.tourService = tourService;
  }

  @PostMapping
  public ResponseEntity<Tour> create(@RequestBody @Validated TourRequest body) {
    var createdTour = tourService.createTour(body);
    var uri = URI.create(String.format("/api/v1/tours/%d", createdTour.id()));
    return ResponseEntity.created(uri).body(createdTour);
  }
}
