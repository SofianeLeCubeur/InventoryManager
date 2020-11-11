package com.github.sofiman.inventory.database;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.LocationPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Database {

    public abstract void connect();
    public abstract void disconnect();

    public abstract void fetchInventories(final DatabaseResponse<List<Inventory>> callback);
    public abstract void fetchInventory(final byte[] id, final DatabaseResponse<Inventory> callback);
    public abstract void pushInventory(final byte[] id, Map<String, String> params, List<String> items, String icon, String background, final DatabaseResponse<Inventory> callback);
    public abstract void createInventory(Map<String, String> params, List<String> items, String icon, String background, final DatabaseResponse<Inventory> callback);

    public abstract void fetchItems(List<String> items, final DatabaseResponse<List<Item>> callback);
    public abstract void fetchItem(byte[] itemId, final DatabaseResponse<Item> callback);
    public abstract void createItem(Map<String, String> params, String icon, String background, final DatabaseResponse<Item> callback);
    public abstract void pushItem(final byte[] id, Map<String, String> params, List<LocationPoint> locations, String icon, String background, final DatabaseResponse<Item> callback);

    public  abstract void fetchContainers(final DatabaseResponse<List<Container>> callback);
    public abstract void  pushContainer(final byte[] id, Map<String, String> params, List<String> items,
                                       List<LocationPoint> locations, String icon, String background, final DatabaseResponse<Container> callback);
    public  abstract void fetchContainer(byte[] containerId, final DatabaseResponse<Container> callback);
    public  abstract void createContainer(Map<String, String> params, List<String> items, final DatabaseResponse<Container> callback);

    public abstract void fetchScanResult(String objectId, String location, final DatabaseResponse<HashMap<String, Object>> callback);
    public abstract void doQuery(HashMap<String, String> filters, final DatabaseResponse<HashMap<String, Object>> callback);

}
