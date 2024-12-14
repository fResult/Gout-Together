package dev.fResult.goutTogether.common.exceptions;

public class BookingExistsException extends RuntimeException {
  public BookingExistsException() {
    super();
  }

  public BookingExistsException(String message) {
    super(message);
  }
}
