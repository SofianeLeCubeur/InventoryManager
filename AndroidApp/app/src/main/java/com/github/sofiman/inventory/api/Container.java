package com.github.sofiman.inventory.api;

import java.util.List;

public class Container {

    private final String id;
    private String content;
    private String location;
    private List<LocationPoint> locations;
    private String details;
    private String state;
    private List<String> items;

    public Container(byte[] id, String content, String location, String details, String state) {
        this.id = ID.toHex(id);
        this.content = content;
        this.location = location;
        this.details = details;
        this.state = state;
    }

    public Container setLocations(List<LocationPoint> locations) {
        this.locations = locations;

        return this;
    }

    public String getRawId(){
        return id;
    }

    public byte[] getId() {
        return ID.fromHex(id);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Container setItems(List<String> items) {
        this.items = items;

        return this;
    }

    public List<String> getItems() {
        return items;
    }

    public List<LocationPoint> getLocations() {
        return locations;
    }

    @Override
    public String toString() {
        return "Container{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", location='" + location + '\'' +
                ", locations=" + locations +
                ", details='" + details + '\'' +
                ", state='" + state + '\'' +
                ", items=" + items +
                '}';
    }
}
