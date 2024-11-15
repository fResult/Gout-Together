package dev.fResult.goutTogether.tourCompanies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tour-companies")
public class TourCompanyController {
    private final Logger logger = LoggerFactory.getLogger(TourCompanyController.class);

    private TourCompanyServiceImpl tourCompanyService;

    public TourCompanyController(TourCompanyServiceImpl tourCompanyService) {
        this.tourCompanyService = tourCompanyService;
    }
}
