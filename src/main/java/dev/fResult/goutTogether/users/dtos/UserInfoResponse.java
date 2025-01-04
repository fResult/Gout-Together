package dev.fResult.goutTogether.users.dtos;

import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.users.entities.User;
import java.util.Map;
import java.util.function.Function;

public record UserInfoResponse(
    Integer id, String firstName, String lastName, String email, String phoneNumber) {
  public static UserInfoResponse of(
      Integer id, String firstName, String lastName, String email, String phoneNumber) {
    return new UserInfoResponse(id, firstName, lastName, email, phoneNumber);
  }

  public static UserInfoResponse fromUserDao(User user) {
    return new UserInfoResponse(
        user.id(), user.firstName(), user.lastName(), null, user.phoneNumber());
  }

  public static UserInfoResponse fromUserDao(User user, UserLogin userLogin) {
    return new UserInfoResponse(
        user.id(), user.firstName(), user.lastName(), userLogin.email(), user.phoneNumber());
  }

  public static Function<User, UserInfoResponse> fromUserDaoWithUserCredentialMap(
      Map<Integer, UserLogin> userIdToCredentialMap) {

    return user -> {
      final var credential = userIdToCredentialMap.get(user.id());

      return new UserInfoResponse(
          user.id(), user.firstName(), user.lastName(), credential.email(), user.phoneNumber());
    };
  }
}
