package dev.fResult.goutTogether.users.dtos;

import dev.fResult.goutTogether.users.entities.User;

public record UserInfoResponse(Integer id, String firstName, String lastName, String phoneNumber) {
  public static UserInfoResponse of(
      Integer id, String firstName, String lastName, String phoneNumber) {
    return new UserInfoResponse(id, firstName, lastName, phoneNumber);
  }

  public static UserInfoResponse fromDao(User user) {
    return new UserInfoResponse(user.id(), user.firstName(), user.lastName(), user.phoneNumber());
  }
}
