package dev.fResult.goutTogether.common.exceptions;

public class InsufficientTourCountException extends ValidationException {
  public InsufficientTourCountException() {
    super("Insufficient tour count");
  }

  public InsufficientTourCountException(String message) {
    super(message);
  }
}
