package dev.fResult.goutTogether.users;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("roles")
public record Role(@Id Integer id, String name) {
  public static Role of(Integer id, String name) {
    return new Role(id, name);
  }
}
