package dev.fResult.goutTogether.tourCompanies.models;

import jakarta.validation.constraints.NotBlank;

public record TourCompanyRequest(
        Integer id,
        @NotBlank String name,
        String status) {
    public static TourCompanyRequest of(Integer id, String name, String status) {
        return new TourCompanyRequest(id, name, status);
    }
}
