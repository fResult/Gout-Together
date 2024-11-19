package dev.fResult.goutTogether.tourCompanies;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.tourCompanies.dtos.RegisterTourCompanyRequest;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(TourCompanyController.class)
class TourCompanyControllerTest {
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private TourCompanyService tourCompanyService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    System.out.println("Setting up");
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenRegisterTourCompanyThenSuccess() throws Exception {
    // Arrange
    var TOUR_ID = 1;
    var body = RegisterTourCompanyRequest.of(null, "My Tour", "MyTour", "mypassword", null);
    var mockTourCompany = TourCompany.of(TOUR_ID, "My Tour", TourCompanyStatus.WAITING.name());
    when(tourCompanyService.registerTourCompany(any(RegisterTourCompanyRequest.class)))
        .thenReturn(mockTourCompany);

    // Actual
    var resultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/tour-companies")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(TOUR_ID));
  }
}
