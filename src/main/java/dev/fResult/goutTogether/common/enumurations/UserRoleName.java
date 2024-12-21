package dev.fResult.goutTogether.common.enumurations;

public enum UserRoleName {
  ADMIN(1),
  CONSUMER(2),
  COMPANY(3);

  private final int id;

  UserRoleName(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
