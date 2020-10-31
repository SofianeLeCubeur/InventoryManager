package com.github.sofiman.inventory.api;

public class Tracker {

    private String name;
    private String location;
    private String icon;

    public Tracker(String name, String location, String icon) {
        this.name = name;
        this.location = location;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
