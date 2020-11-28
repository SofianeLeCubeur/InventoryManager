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
    private List<Webhook> webhooks;

    public Inventory(byte[] id, String name, String icon, String location, List<String> items, List<Webhook> webhooks) {
        this.id = ID.toHex(id);
        this.name = name;
        this.icon = icon;
        this.webhooks = webhooks;
        this.location = location;
        this.items = items;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getState() {
        return state;
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

    public List<Webhook> getWebhooks() {
        return webhooks;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", webhooks=" + webhooks +
                ", location='" + location + '\'' +
                ", state='" + state + '\'' +
                ", items=" + items +
                '}';
    }
}
