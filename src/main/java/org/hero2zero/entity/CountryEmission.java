package org.hero2zero.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CountryEmission")
public class CountryEmission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String countryCode;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private double co2Emissions;

    // Getter & Setter...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getCo2Emissions() { return co2Emissions; }
    public void setCo2Emissions(double co2Emissions) { this.co2Emissions = co2Emissions; }
}
