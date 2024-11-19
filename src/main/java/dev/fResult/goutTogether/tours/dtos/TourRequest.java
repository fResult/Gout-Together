package dev.fResult.goutTogether.tours.dtos;

import dev.fResult.goutTogether.common.enumurations.TourStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TourRequest(
    @NotNull Integer tourCompanyId,
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String location,
    @NotNull Integer numberOfPeople,
    @NotNull Instant activityDate,
    TourStatus status) {

  public static TourRequest of(
      Integer tourCompanyId,
      String title,
      String description,
      String location,
      int numberOfPeople,
      Instant activityDate,
      TourStatus status) {
    return new TourRequest(
        tourCompanyId, title, description, location, numberOfPeople, activityDate, status);
  }
}
