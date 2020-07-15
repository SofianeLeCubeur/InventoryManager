package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.models.Container;
import com.github.sofiman.inventory.api.models.Item;
import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.utils.ID;

import java.util.*;
import java.util.stream.Collectors;

class ContainerImpl extends CachedModel<Item> implements Container, Child {

    private final byte[] id;
    private final Map<byte[], Item> items;
    private final Map<Long, byte[]> registry;
    private final String contentDescription;
    private String details;
    private String state;
    private Container parentContainer;
    private List<LocationPoint> locations;

    ContainerImpl(byte[] id, String contentDescription) {
        this.id = id;
        this.contentDescription = contentDescription;
        this.items = new HashMap<>();
        this.registry = new HashMap<>();
    }

    @Override
    public byte[] getId() {
        return id;
    }

    @Override
    public String getCode() {
        return ID.toHex(id);
    }

    @Override
    public String getContentDescription() {
        return contentDescription;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public LocationPoint getCurrentLocation() {
        return locations.get(0);
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public Container getParent() {
        return parentContainer;
    }

    @Override
    public Container pushLocation(LocationPoint location) {
        this.locations.add(location);
        this.locations = this.locations.stream().sorted(Comparator.comparingLong(LocationPoint::getTimestamp)).collect(Collectors.toList());
        return this;
    }

    protected void setLocationHistory(List<LocationPoint> locationHistory){
        this.locations = locationHistory.stream().sorted(Comparator.comparingLong(LocationPoint::getTimestamp)).collect(Collectors.toList());
    }

    @Override
    public Container setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public Container setDetails(String details) {
        this.details = details;
        return this;
    }

    @Override
    public void register(Item item) {
        put(item, System.currentTimeMillis());
    }

    @Override
    public Item pull(byte[] id) {
        if(!registry.containsValue(id)) return null;
        final Item item = get(id);
        remove(id);
        items.remove(id);
        return item;
    }

    protected void put(Item item, long registrationDate){
        ((ItemImpl) item).assign(this);
        this.items.put(item.getId(), item);
        this.registry.put(registrationDate, item.getId());
    }

    @Override
    public Collection<Item> pullItems() {
        return items.values();
    }

    @Override
    public List<LocationPoint> getLocationHistory() {
        return locations;
    }

    protected void setRegisteredItems(Map<Long, byte[]> registeredItems){
        this.registry.clear();
        this.registry.putAll(registeredItems);
    }

    @Override
    public Map<Long, byte[]> getRegisteredItems() {
        return registry;
    }

    @Override
    public Item fetch(byte[] id) {
        if(!registry.containsValue(id)) return null;
        Item item = get(id);
        if(item == null){
            item = database.findItem(InventoryManager.ID_BIN_FILTER.apply(id));
            put(id, item, database.getItemCacheExpire());
        }
        return item;
    }

    @Override
    public void assign(Container parent) {
        this.parentContainer = parent;
    }

    @Override
    public void dismiss() {
        this.parentContainer = null;
    }

    @Override
    public String toString() {
        return "Container{" +
                "id=" + getCode() +
                ", items=" + items +
                ", contentDescription='" + contentDescription + '\'' +
                ", details='" + details + '\'' +
                ", locations='" + getLocationHistory() + '\'' +
                ", state='" + state + '\'' +
                (parentContainer != null ? ", parentContainerId=" + parentContainer.getCode() : "") +
                '}';
    }
}
