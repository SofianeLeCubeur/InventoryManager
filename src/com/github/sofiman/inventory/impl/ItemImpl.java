package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.models.Container;
import com.github.sofiman.inventory.api.models.Item;
import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.api.Tag;
import com.github.sofiman.inventory.utils.ID;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class ItemImpl extends CachedModel<Container> implements Item, Child {

    private final byte[] id;
    private final String name;
    private final String internalName;
    private final String serialNumber;
    private final String description;
    private String state;
    private String details;
    private List<Tag> tags;
    private byte[] parentId;
    private List<LocationPoint> locations;

    public ItemImpl(byte[] id, String name, String internalName, String serialNumber, String description) {
        this.id = id;
        this.name = name;
        this.internalName = internalName;
        this.serialNumber = serialNumber;
        this.description = description;
        this.locations = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    @Override
    public byte[] getId() {
        return id;
    }

    @Override
    public String getCode() {
        return ID.toHex(this.id);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public LocationPoint getCurrentLocation() {
        return locations.get(0);
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public Container getParent() {
        Container container = get(id);
        if(container == null && parentId != null){
            container = database.findContainer(InventoryManager.ID_BIN_FILTER.apply(parentId));
            put(id, container, database.getItemCacheExpire());
        }
        return container;
    }

    @Override
    public Item addTag(Tag tag) {
        this.tags.add(tag);
        return this;
    }

    @Override
    public Item removeTag(Tag tag) {
        this.tags.remove(tag);
        return this;
    }

    @Override
    public Item setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public Item pushLocation(LocationPoint location) {
        this.locations.add(location);
        this.locations = this.locations.stream().sorted(Comparator.comparingLong(LocationPoint::getTimestamp)).collect(Collectors.toList());
        return this;
    }

    protected void setLocationHistory(List<LocationPoint> locationHistory){
        this.locations = locationHistory.stream().sorted(Comparator.comparingLong(LocationPoint::getTimestamp)).collect(Collectors.toList());
    }

    protected void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public Item setDetails(String details) {
        this.details = details;
        return this;
    }

    @Override
    public List<LocationPoint> getLocationHistory() {
        return new ArrayList<>(locations);
    }

    public void assign(Container container){
        if(container == null) return;
        this.parentId = container.getId();
        put(id, container, database.getItemCacheExpire());
    }

    public void dismiss(){
        this.parentId = null;
        remove(id);
    }

    protected void setParentId(byte[] id){
        if(id != null && id[0] == InventoryManager.TYPE_CONTAINER){
            this.parentId = id;
        }
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + getCode() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", state='" + state + '\'' +
                ", locations='" + locations.toString() + '\'' +
                ", details='" + details + '\'' +
                ", tags=" + tags +
                (parentId != null ? ", parentContainerID=" + ID.toHex(parentId) : "") +
                '}';
    }
}
