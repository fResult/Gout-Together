package dev.fResult.goutTogether.tours.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TourRequest(
    @NotNull Integer tourCompanyId,
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String location,
    int numberOfPeople,
    @NotNull Instant activityDate,
    String status) {

  public TourRequest of(
      Integer tourCompanyId,
      String title,
      String description,
      String location,
      int numberOfPeople,
      Instant activityDate,
      String status) {
    return new TourRequest(
        tourCompanyId, title, description, location, numberOfPeople, activityDate, status);
  }
}
