package com.github.sofiman.inventory.api;

import java.util.List;

public class Inventory {

    private final String id;
    private String name;
    private String icon;
    private String background;
    private String location;
    private String state;
    private List<String> items;
    private List<String> trackers;

    private int trackerCount;

    public Inventory(byte[] id, String name, String icon, String location, List<String> items, List<String> trackers) {
        this.id = ID.toHex(id);
        this.name = name;
        this.icon = icon;
        this.trackerCount = 0;
        this.location = location;
        this.items = items;
        this.trackers = trackers;
    }

    public byte[] getId() {
        return ID.fromHex(id);
    }

    public String getRawId(){
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getItemCount() {
        return items.size();
    }

    public int getTrackerCount() {
        return trackerCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<String> getTrackers() {
        return trackers;
    }

    public void setTrackers(List<String> trackers) {
        this.trackers = trackers;
    }

    public List<String> getItems() {
        return items;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getBackground() {
        return background;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", trackerCount=" + trackerCount +
                ", location='" + location + '\'' +
                ", state='" + state + '\'' +
                ", items=" + items +
                '}';
    }
}
