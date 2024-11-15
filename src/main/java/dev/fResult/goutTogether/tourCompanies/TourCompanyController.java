package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.tourCompanies.models.TourCompany;
import dev.fResult.goutTogether.tourCompanies.models.TourCompanyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tour-companies")
public class TourCompanyController {
    private final Logger logger = LoggerFactory.getLogger(TourCompanyController.class);
    private final TourCompanyServiceImpl tourCompanyService;

    public TourCompanyController(TourCompanyServiceImpl tourCompanyService) {
        this.tourCompanyService = tourCompanyService;
    }

    @PostMapping
    public ResponseEntity<TourCompany> register(@RequestBody TourCompanyRequest body) {
        logger.info("Registering a new tour company");
        return ResponseEntity.created(URI.create("/api/v1/tour-companies"))
                .body(tourCompanyService.registerTourCompany(body));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TourCompany> approve(@PathVariable int id) {
        logger.info("Approving a new tour company");
        try {
            logger.info("Approving a new tour company with id: {}", id);
            var approvedCompany = tourCompanyService.approveTourCompany(id);
            logger.info("[approvedCompany] company id: {} is approved", id);
            return ResponseEntity.ok(approvedCompany);
        } catch (Exception e) {
            var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
            return ResponseEntity.of(detail).build();
        }
    }
}
