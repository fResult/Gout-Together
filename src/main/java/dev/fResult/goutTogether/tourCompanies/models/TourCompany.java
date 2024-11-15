package dev.fResult.goutTogether.tourCompanies.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("tour_companies")
public record TourCompany(@Id Integer id, String name, String status) {
    public static TourCompany of(Integer id, String name, String status) {
        return new TourCompany(id, name, status);
    }
}
