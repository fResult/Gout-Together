package dev.fResult.goutTogether.common.exceptions;

public class InsufficientBalanceException extends ValidationException {
  public InsufficientBalanceException() {
    super("Insufficient balance");
  }

  public InsufficientBalanceException(String message) {
    super(message);
  }
}
