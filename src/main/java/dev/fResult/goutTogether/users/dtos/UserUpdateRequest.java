package dev.fResult.goutTogether.users.dtos;

import dev.fResult.goutTogether.users.entities.User;
import java.util.Optional;
import java.util.function.Function;

public record UserUpdateRequest(String firstName, String lastName, String phoneNumber) {
  public static UserUpdateRequest of(String firstName, String lastName, String phoneNumber) {
    return new UserUpdateRequest(firstName, lastName, phoneNumber);
  }

  public static Function<User, User> dtoToUserUpdate(UserUpdateRequest body) {
    return user ->
        User.of(
            user.id(),
            Optional.ofNullable(body.firstName()).orElse(user.firstName()),
            Optional.ofNullable(body.lastName()).orElse(user.lastName()),
            Optional.ofNullable(body.phoneNumber()).orElse(user.phoneNumber()));
  }
}
