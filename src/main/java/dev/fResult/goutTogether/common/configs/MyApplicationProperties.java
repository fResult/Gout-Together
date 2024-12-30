package dev.fResult.goutTogether.common.configs;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goutapp")
public class MyApplicationProperties {
  private final Booking booking = new Booking();

  public Booking getBooking() {
    return booking;
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
}
