package dev.fResult.goutTogether.users.dtos;

public record UserUpdateRequest(
    String firstName, String lastName, String email, String phoneNumber) {
  public static UserUpdateRequest of(
      String firstName, String lastName, String email, String phoneNumber) {
    return new UserUpdateRequest(firstName, lastName, email, phoneNumber);
  }
}
