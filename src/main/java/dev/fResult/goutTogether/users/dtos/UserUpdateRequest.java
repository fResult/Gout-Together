package dev.fResult.goutTogether.users.dtos;

public record UserUpdateRequest(String firstName, String lastName, String phoneNumber) {
  public static UserUpdateRequest of(String firstName, String lastName, String phoneNumber) {
    return new UserUpdateRequest(firstName, lastName, phoneNumber);
  }
}
