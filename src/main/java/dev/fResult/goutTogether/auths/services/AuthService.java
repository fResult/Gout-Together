package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import java.util.*;

public interface AuthService {
  List<UserLogin> findUserCredentialsByUserIds(Collection<Integer> userIds);

  Optional<UserLogin> findUserCredentialByEmail(String userEmail);

  UserLogin findUserCredentialByUserId(int id);

  UserLogin createUserLogin(int userId, String email, String password);

  Optional<TourCompanyLogin> findTourCompanyCredentialByUsername(String username);

  TourCompanyLogin findTourCompanyCredentialByTourCompanyId(int id);

  TourCompanyLogin createTourCompanyLogin(int tourCompanyId, String username, String password);
}
