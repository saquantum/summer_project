package com.ourrainwater.pojo;

public class Location {
    private int id;
    private String city;
    private String county;

    public Location() {
    }

    public Location(String city, String county) {
        this.city = city;
        this.county = county;
    }

    public Location(int id, String city, String county) {
        this.id = id;
        this.city = city;
        this.county = county;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", city='" + city + '\'' +
                ", county='" + county + '\'' +
                '}';
    }
}

