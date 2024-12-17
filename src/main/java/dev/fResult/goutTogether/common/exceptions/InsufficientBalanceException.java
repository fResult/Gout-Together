package dev.fResult.goutTogether.common.exceptions;

public class InsufficientBalanceException extends RuntimeException {
  public InsufficientBalanceException() {
    super("Insufficient balance");
  }

  public InsufficientBalanceException(String message) {
    super(message);
  }
}
