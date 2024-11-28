package dev.fResult.goutTogether.common.enumurations;

public enum UserRoleName {
  CONSUMER(1),
  ADMIN(2),
  COMPANY(3);

  private final int id;

  UserRoleName(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
