package com.github.sofiman.inventory.api.models;

import com.github.sofiman.inventory.api.Query;

import java.util.List;
import java.util.Set;

public interface Inventory extends Identifiable {

    String getIcon();
    String getName();
    String getState();
    String getLocation();
    int getItemCount();

    List<Item> query(Query query);
    Set<byte[]> getRegisteredItemIds();
    Item fetch(byte[] id);
    Item pull(byte[] id);
    void push(Item item);

    Inventory setState(String state);
    Inventory setLocation(String location);
}
