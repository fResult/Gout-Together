package dev.fResult.goutTogether.wallets;

import static dev.fResult.goutTogether.common.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.wallets.dtos.TourCompanyWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.dtos.WalletWithdrawRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(WalletController.class)
class WalletControllerTest {
  private final String WALLET_API = "/api/v1/wallets";
  private final int USER_ID = 1;
  private final int TOUR_COMPANY_ID = 2;
  private final String IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private WalletService walletService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  private Authentication buildAuthentication(int resourceId, UserRoleName roleName) {
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim(RESOURCE_ID_CLAIM, String.valueOf(resourceId))
            .claim(ROLES_CLAIM, List.of("ROLE_" + roleName.name()))
            .build();
    return new JwtAuthenticationToken(jwt);
  }

  @Test
  void whenGetMyUserWallet_ThenSuccess() throws Exception {
    // Arrange
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER);
    var userWallet =
        UserWallet.of(1, AggregateReference.to(USER_ID), Instant.now(), BigDecimal.TEN);
    var expectedUserWalletInfo =
        UserWalletInfoResponse.of(userWallet.id(), USER_ID, userWallet.balance());
    when(walletService.getConsumerWalletInfoByUserId(anyInt())).thenReturn(expectedUserWalletInfo);

    // Actual
    var resultActions = mockMvc.perform(get(WALLET_API + "/me").principal(authentication));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(USER_ID));
  }

  @Test
  void whenTopUpUserWallet_ThenSuccess() throws Exception {
    // Arrange
    var AMOUNT_TO_TOP_UP = BigDecimal.valueOf(300);
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER);
    var body = WalletTopUpRequest.of(AMOUNT_TO_TOP_UP);
    var expectedBalanceAfterTopUp = AMOUNT_TO_TOP_UP.add(BigDecimal.valueOf(100));
    var expectedUserWalletInfo = UserWalletInfoResponse.of(1, USER_ID, expectedBalanceAfterTopUp);

    when(walletService.topUpConsumerWallet(USER_ID, IDEMPOTENT_KEY, body))
        .thenReturn(expectedUserWalletInfo);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(WALLET_API + "/top-up")
                .principal(authentication)
                .header("idempotent-key", IDEMPOTENT_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(USER_ID))
        .andExpect(jsonPath("$.balance").value(expectedBalanceAfterTopUp));
  }

  @Test
  void whenGetMyTourCompanyWallet_ThenSuccess() throws Exception {
    // Arrange
    var BALANCE = BigDecimal.valueOf(1_000_000);
    var authentication = buildAuthentication(TOUR_COMPANY_ID, UserRoleName.COMPANY);
    var expectedCompanyWalletInfo = TourCompanyWalletInfoResponse.of(3, TOUR_COMPANY_ID, BALANCE);

    when(walletService.getTourCompanyWalletInfoByTourCompanyId(TOUR_COMPANY_ID))
        .thenReturn(expectedCompanyWalletInfo);

    // Actual
    var resultActions = mockMvc.perform(get(WALLET_API + "/my-company").principal(authentication));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tourCompanyId").value(TOUR_COMPANY_ID))
        .andExpect(jsonPath("$.balance").value(BALANCE));
  }

  @Test
  void whenWithdrawMoney_ThenSuccess() throws Exception {
    // Arrange
    var AMOUNT_TO_WITHDRAW = BigDecimal.valueOf(80_000);
    var BALANCE_AFTER_WITHDRAW = BigDecimal.valueOf(920_000);
    var authentication = buildAuthentication(TOUR_COMPANY_ID, UserRoleName.COMPANY);
    var body = WalletWithdrawRequest.of(AMOUNT_TO_WITHDRAW);
    var expectedCompanyWalletInfo =
        TourCompanyWalletInfoResponse.of(2, TOUR_COMPANY_ID, BALANCE_AFTER_WITHDRAW);

    when(walletService.withdrawTourCompanyWallet(
            anyInt(), anyString(), any(WalletWithdrawRequest.class)))
        .thenReturn(expectedCompanyWalletInfo);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(WALLET_API + "/withdraw")
                .header("idempotent-key", IDEMPOTENT_KEY)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tourCompanyId").value(TOUR_COMPANY_ID))
        .andExpect(jsonPath("$.balance").value(BALANCE_AFTER_WITHDRAW));
  }
}
