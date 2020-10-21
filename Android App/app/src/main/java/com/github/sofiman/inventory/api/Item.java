package com.github.sofiman.inventory.api;

import java.util.List;

public class Item {

    private final String id;
    private String name;
    private String icon;
    private String background;
    private String reference;
    private String serial_number;
    private String description;
    private String state;
    private List<LocationPoint> locations;
    private String details;

    public Item(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Item(String id, String name, String icon, String reference, String serial_number, String description, String state, List<LocationPoint> locations, String details) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.reference = reference;
        this.serial_number = serial_number;
        this.description = description;
        this.state = state;
        this.locations = locations;
        this.details = details;
    }

    public byte[] getId() {
        return ID.fromHex(id);
    }

    public String getRawId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getIcon() {
        return icon;
    }

    public String getInternalName() {
        return reference;
    }

    public void setInternalName(String internalName) {
        this.reference = internalName;
    }

    public String getSerialNumber() {
        return serial_number;
    }

    public void setSerialNumber(String serialNumber) {
        this.serial_number = serialNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Item setLocations(List<LocationPoint> locations) {
        this.locations = locations;
        return this;
    }

    public List<LocationPoint> getLocations() {
        return locations;
    }

    public String getDetails() {
        return details;
    }

    public Item setDetails(String details) {
        this.details = details;
        return this;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", internalName='" + reference + '\'' +
                ", serialNumber='" + serial_number + '\'' +
                ", description='" + description + '\'' +
                ", state='" + state + '\'' +
                ", locations='" + locations + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
