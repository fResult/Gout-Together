package dev.fResult.goutTogether.common.exceptions;

public class EntityNotFound extends RuntimeException {
  public EntityNotFound() {
    super("Entity not found");
  }

  public EntityNotFound(String message) {
    super(message);
  }
}
