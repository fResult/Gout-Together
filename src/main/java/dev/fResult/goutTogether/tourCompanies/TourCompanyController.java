package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.tourCompanies.models.TourCompany;
import dev.fResult.goutTogether.tourCompanies.models.TourCompanyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tour-companies")
public class TourCompanyController {
    private final Logger logger = LoggerFactory.getLogger(TourCompanyController.class);

    private TourCompanyServiceImpl tourCompanyService;

    public TourCompanyController(TourCompanyServiceImpl tourCompanyService) {
        this.tourCompanyService = tourCompanyService;
    }

    @PostMapping
    public ResponseEntity<TourCompany> register(@RequestBody TourCompanyRequest body) {
        logger.info("Registering a new tour company");
        return ResponseEntity.created(URI.create("/api/v1/tour-companies"))
                .body(tourCompanyService.registerTour(body));
    }
}
