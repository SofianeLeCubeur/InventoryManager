package com.github.sofiman.inventory.api;

public final class LocationPoint {

    private final long timestamp;
    private final String location;

    public LocationPoint(long timestamp, String location) {
        this.timestamp = timestamp;
        this.location = location;
    }

    public LocationPoint(String location){
        this.timestamp = System.currentTimeMillis();
        this.location = location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "LocationPoint{" +
                "timestamp=" + timestamp +
                ", location='" + location + '\'' +
                '}';
    }
}
