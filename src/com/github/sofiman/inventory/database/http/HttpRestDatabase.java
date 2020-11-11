package com.github.sofiman.inventory.database.http;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.database.Database;
import com.github.sofiman.inventory.database.DatabaseResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRestDatabase extends Database {
    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void fetchInventories(DatabaseResponse<List<Inventory>> callback) {

    }

    @Override
    public void fetchInventory(byte[] id, DatabaseResponse<Inventory> callback) {

    }

    @Override
    public void pushInventory(byte[] id, Map<String, String> params, List<String> items, String icon, String background, DatabaseResponse<Inventory> callback) {

    }

    @Override
    public void createInventory(Map<String, String> params, List<String> items, String icon, String background, DatabaseResponse<Inventory> callback) {

    }

    @Override
    public void fetchItems(List<String> items, DatabaseResponse<List<Item>> callback) {

    }

    @Override
    public void fetchItem(byte[] itemId, DatabaseResponse<Item> callback) {

    }

    @Override
    public void createItem(Map<String, String> params, String icon, String background, DatabaseResponse<Item> callback) {

    }

    @Override
    public void pushItem(byte[] id, Map<String, String> params, List<LocationPoint> locations, String icon, String background, DatabaseResponse<Item> callback) {

    }

    @Override
    public void fetchContainers(DatabaseResponse<List<Container>> callback) {

    }

    @Override
    public void pushContainer(byte[] id, Map<String, String> params, List<String> items, List<LocationPoint> locations, String icon, String background, DatabaseResponse<Container> callback) {

    }

    @Override
    public void fetchContainer(byte[] containerId, DatabaseResponse<Container> callback) {

    }

    @Override
    public void createContainer(Map<String, String> params, List<String> items, DatabaseResponse<Container> callback) {

    }

    @Override
    public void fetchScanResult(String objectId, String location, DatabaseResponse<HashMap<String, Object>> callback) {

    }

    @Override
    public void doQuery(HashMap<String, String> filters, DatabaseResponse<HashMap<String, Object>> callback) {

    }
}
