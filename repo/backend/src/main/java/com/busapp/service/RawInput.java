package com.busapp.service;

public class RawInput {
    private String name;
    private String address;
    private String residentialArea;
    private String apartmentType;
    private Double area;
    private String unit;
    private String price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "RawInput{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", residentialArea='" + residentialArea + '\'' +
                ", apartmentType='" + apartmentType + '\'' +
                ", area=" + area +
                ", unit='" + unit + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
