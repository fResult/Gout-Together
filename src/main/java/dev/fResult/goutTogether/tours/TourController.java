package dev.fResult.goutTogether.tours;

import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import dev.fResult.goutTogether.tours.dtos.TourRequest;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourService;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

  @GetMapping
  public ResponseEntity<Page<Tour>> getTours(
      @RequestParam(required = true) int page,
      @RequestParam(required = true) int size,
      @RequestParam(defaultValue = "id") String field,
      @RequestParam(defaultValue = "ASCC") Sort.Direction direction) {
     var sort =  Sort.by(direction, field);

//    var sort = Sort.by(Sort.Direction.fromString(direction.name()), field);
    var pageable = PageRequest.of(page, size, sort);
    var tours = tourService.getTours(pageable);

    return ResponseEntity.ok(tours);
  }

  @PostMapping
  public ResponseEntity<Tour> create(@RequestBody @Validated TourRequest body) {
    var createdTour = tourService.createTour(body);
    var uri = URI.create(String.format("/api/v1/tours/%d", createdTour.id()));
    return ResponseEntity.created(uri).body(createdTour);
  }
}
