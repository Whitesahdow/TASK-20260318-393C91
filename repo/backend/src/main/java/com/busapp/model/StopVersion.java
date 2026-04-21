package com.busapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "stop_version")
public class StopVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stopName;

    private String address;
    private String residentialArea;
    private String apartmentType;
    private Double areaSqm;
    private Double priceYuanMonth;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawSource;

    @Column(nullable = false)
    private LocalDateTime importedAt;

    @Column(nullable = false)
    private Integer versionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getResidentialArea() {
        return residentialArea;
    }

    public void setResidentialArea(String residentialArea) {
        this.residentialArea = residentialArea;
    }

    public String getApartmentType() {
        return apartmentType;
    }

    public void setApartmentType(String apartmentType) {
        this.apartmentType = apartmentType;
    }

    public Double getAreaSqm() {
        return areaSqm;
    }

    public void setAreaSqm(Double areaSqm) {
        this.areaSqm = areaSqm;
    }

    public Double getPriceYuanMonth() {
        return priceYuanMonth;
    }

    public void setPriceYuanMonth(Double priceYuanMonth) {
        this.priceYuanMonth = priceYuanMonth;
    }

    public String getRawSource() {
        return rawSource;
    }

    public void setRawSource(String rawSource) {
        this.rawSource = rawSource;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }
}
