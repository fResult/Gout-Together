package dev.fResult.goutTogether.tours;

import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import dev.fResult.goutTogether.tours.repositories.TourRepository;
import dev.fResult.goutTogether.tours.services.TourCountService;
import dev.fResult.goutTogether.tours.services.TourServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {
  @InjectMocks private TourServiceImpl tourService;

  @Mock private TourRepository tourRepository;
  @Mock private TourCompanyService tourCompanyService;
  @Mock private TourCountService tourCountService;
}
