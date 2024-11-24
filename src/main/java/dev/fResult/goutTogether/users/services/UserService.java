package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.auths.UserForgotPasswordRequest;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import java.util.List;

public interface UserService {
  List<UserInfoResponse> getUsers();

  UserInfoResponse getUserById(int id);

  UserInfoResponse register(UserRegistrationRequest user);

  UserInfoResponse updateUserById(int id, UserUpdateRequest user);

  void deleteUserById(int id);

  UpdatePasswordResult changePassword(UserForgotPasswordRequest body);
}
