package dev.fResult.goutTogether.users.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record User(@Id Integer id, String firstName, String lastName, String phoneNumber) {
  public static User of(Integer id, String firstName, String lastName, String phoneNumber) {
    return new User(id, firstName, lastName, phoneNumber);
  }
}
