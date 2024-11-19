package dev.fResult.goutTogether.common.enumurations;

public enum UserRole {
  CONSUMER(1),
  ADMIN(2),
  COMPANY(3);

  private final int id;

  UserRole(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
