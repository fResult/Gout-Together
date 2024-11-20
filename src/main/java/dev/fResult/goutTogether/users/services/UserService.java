package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.auths.UserForgotPasswordRequest;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import java.util.List;

public interface UserService {
  List<User> getUsers();

  UserInfoResponse getUserById(int id);

  UserInfoResponse register(UserRegistrationRequest user);

  UserInfoResponse updateUser(int id, UserUpdateRequest user);

  void deleteUser(int id);

  UpdatePasswordResult changePassword(UserForgotPasswordRequest body);
}
