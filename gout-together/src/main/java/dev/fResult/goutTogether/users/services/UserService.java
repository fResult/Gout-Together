package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.auths.dtos.UserChangePasswordRequest;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
  Page<UserInfoResponse> getUsersByFirstName(String keyword, Pageable pageable);

  UserInfoResponse getUserById(int id);

  UserInfoResponse registerUser(UserRegistrationRequest user);

  UserInfoResponse updateUserById(int id, UserUpdateRequest user);

  UpdatePasswordResult changePasswordByUserId(int id, UserChangePasswordRequest body);

  UpdatePasswordResult changePassword(String email, UserChangePasswordRequest body);

  boolean deleteUserById(int id);
}
