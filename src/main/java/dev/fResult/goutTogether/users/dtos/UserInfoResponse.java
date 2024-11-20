package dev.fResult.goutTogether.users.dtos;

public record UserInfoResponse(Integer id, String firstName, String lastName, String phoneNumber) {
  public static UserInfoResponse of(
      Integer id, String firstName, String lastName, String phoneNumber) {
    return new UserInfoResponse(id, firstName, lastName, phoneNumber);
  }
}
