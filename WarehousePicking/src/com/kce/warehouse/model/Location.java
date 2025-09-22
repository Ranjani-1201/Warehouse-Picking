package com.kce.warehouse.model;
public class Location {
    private String locationId;
    private String name;
    public Location(String locationId, String name) {
        this.locationId = locationId;
        this.name = name;
    }
public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name + " (" + locationId + ")";
    }
}
