package dev.fResult.goutTogether.common.configs;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goutapp")
public class MyApplicationProperties {
  private final Booking booking = new Booking();
  private final Token token = new Token();

  public Booking getBooking() {
    return booking;
  }

  public Token getToken() {
    return token;
  }

  public static class Booking {
    /** The static tour price for learning purpose. */
    BigDecimal tourPrice = BigDecimal.valueOf(100.00);

    public BigDecimal getTourPrice() {
      return tourPrice;
    }

    public void setTourPrice(BigDecimal tourPrice) {
      this.tourPrice = tourPrice;
    }
  }

  public static class Token {
    /** Duration of expiration of access token in 'seconds'. (Default: 1 hour) */
    private int accessTokenExpiredInSeconds = 3600;

    /** Duration of expiration of refresh token in 'seconds'. (Default: 1 month) */
    private int refreshTokenExpiredInSeconds = 2592000;

    public int getAccessTokenExpiredInSeconds() {
      return accessTokenExpiredInSeconds;
    }

    public void setAccessTokenExpiredInSeconds(int accessTokenExpiredInSeconds) {
      this.accessTokenExpiredInSeconds = accessTokenExpiredInSeconds;
    }

    public int getRefreshTokenExpiredInSeconds() {
      return refreshTokenExpiredInSeconds;
    }

    public void setRefreshTokenExpiredInSeconds(int refreshTokenExpiredInSeconds) {
      this.refreshTokenExpiredInSeconds = refreshTokenExpiredInSeconds;
    }
  }
}
