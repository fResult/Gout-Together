package dev.fResult.goutTogether.common.exceptions;

public class UnsupportedTransactionTypeException extends RuntimeException {
  public UnsupportedTransactionTypeException() {
    super("Unsupported transaction type");
  }

  public UnsupportedTransactionTypeException(String message) {
    super(message);
  }
}
