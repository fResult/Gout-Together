package dev.fResult.goutTogether.tourCompanies.models;

public record TourCompanyRequest(
        Integer id,
        String name,
        String status) {
    public static TourCompanyRequest of(Integer id, String name, String status) {
        return new TourCompanyRequest(id, name, status);
    }
}
