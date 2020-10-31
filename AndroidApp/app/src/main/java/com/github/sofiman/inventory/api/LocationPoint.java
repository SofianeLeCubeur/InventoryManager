package com.github.sofiman.inventory.api;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationPoint implements Parcelable {

    private String location;
    private long timestamp;
    private double longitude = -1, latitude = -1;

    public LocationPoint(String location, long timestamp) {
        this.location = location;
        this.timestamp = timestamp;
    }

    protected LocationPoint(Parcel in) {
        location = in.readString();
        timestamp = in.readLong();
        longitude = in.readDouble();
        latitude = in.readDouble();
    }

    public static final Creator<LocationPoint> CREATOR = new Creator<LocationPoint>() {
        @Override
        public LocationPoint createFromParcel(Parcel in) {
            return new LocationPoint(in);
        }

        @Override
        public LocationPoint[] newArray(int size) {
            return new LocationPoint[size];
        }
    };

    public long getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(location);
        dest.writeLong(timestamp);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
