package dev.fResult.goutTogether.wallets;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(WalletController.class)
class WalletControllerTest {
  private static final String WALLET_API = "/api/v1/wallets";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private WalletService walletService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenGetMyUserWalletThenSuccess() throws Exception {
    // Arrange
    var USER_ID = 1;
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim(RESOURCE_ID_CLAIM, String.valueOf(USER_ID))
            .claim(ROLES_CLAIM, List.of("ROLE_CONSUMER"))
            .build();
    var authentication = new JwtAuthenticationToken(jwt);

    var userWallet =
        UserWallet.of(1, AggregateReference.to(USER_ID), Instant.now(), BigDecimal.TEN);
    var expectedUserWalletInfo =
        UserWalletInfoResponse.of(userWallet.id(), USER_ID, userWallet.balance());
    when(walletService.getConsumerWalletByUserId(anyInt())).thenReturn(expectedUserWalletInfo);

    // Actual
    var resultActions = mockMvc.perform(get(WALLET_API + "/me").principal(authentication));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(USER_ID));
  }
}
