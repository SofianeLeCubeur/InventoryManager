package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.models.Inventory;
import com.github.sofiman.inventory.api.models.Item;
import com.github.sofiman.inventory.api.Query;
import com.github.sofiman.inventory.utils.ID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class InventoryImpl extends CachedModel<Item> implements Inventory {

    private final byte[] id;
    private final String name;
    private String state;
    private String location;
    private Set<byte[]> items;

    InventoryImpl(byte[] id, String name) {
        this.id = id;
        this.name = name;
        this.items = new HashSet<>();
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
    public String getName() {
        return name;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public List<Item> query(Query query) {
        List<Item> items = database.findItems(query.toBson());
        return items.stream().filter(item -> {
            final byte[] id = item.getId();
            boolean contains = this.items.contains(id);
            if(contains){
                put(id, item, database.getItemCacheExpire());
            }
            return contains;
        }).collect(Collectors.toList());
    }

    @Override
    public Set<byte[]> getRegisteredItemIds() {
        return new HashSet<>(items);
    }

    @Override
    public Item fetch(byte[] id) {
        if(!items.contains(id)) return null;
        Item item = get(id);
        if(item == null){
            item = database.findItem(InventoryManager.ID_BIN_FILTER.apply(id));
            put(id, item, database.getItemCacheExpire());
        }
        return item;
    }

    @Override
    public Item pull(byte[] id) {
        if(!items.contains(id)) return null;
        final Item item = get(id);
        remove(id);
        items.remove(id);
        return item;
    }

    @Override
    public void push(Item item) {
        put(item.getId(), item, database.getItemCacheExpire());
        items.add(item.getId());
    }

    @Override
    public Inventory setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public Inventory setLocation(String location) {
        this.location = location;
        return this;
    }

    public void clearCache(){
        this.clearDataCache();
    }

    protected void setItems(Set<byte[]> itemIds){
        this.items = itemIds;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + getCode() +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", itemIds=" + items +
                '}';
    }
}
