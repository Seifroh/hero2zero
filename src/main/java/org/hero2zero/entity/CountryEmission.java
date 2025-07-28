package org.hero2zero.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "countryemission")
public class CountryEmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "country_code", nullable = true, length = 5)
    private String countryCode;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "co2_emissions", nullable = true)
    private Double co2Emissions;

    @Column(name = "approved", nullable = false)
    private boolean approved = false;

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Double getCo2Emissions() {
        return co2Emissions;
    }

    public void setCo2Emissions(Double co2Emissions) {
        this.co2Emissions = co2Emissions;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public CountryEmission() {

    }

    public CountryEmission(String country, String countryCode) {
        this.country = country;
        this.countryCode = countryCode;
    }
    
    @Column(name = "change_log", nullable = true, length = 500)
    private String changeLog;

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }
}
