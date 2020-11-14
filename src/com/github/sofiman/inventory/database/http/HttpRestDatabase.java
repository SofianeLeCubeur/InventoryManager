package com.github.sofiman.inventory.database.http;

import com.github.sofiman.inventory.api.*;
import com.github.sofiman.inventory.database.Database;
import com.github.sofiman.inventory.database.DatabaseError;
import com.github.sofiman.inventory.database.DatabaseResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRestDatabase extends Database {

    private final String baseUrl;
    private final String username;
    private final String password;
    private Retrofit retrofit;
    private InventoryService inventoryService;
    private Token token;

    public HttpRestDatabase(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public void connect() {
        this.retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.token = null;
        this.inventoryService = retrofit.create(InventoryService.class);
        final String scope = "fetch.* add.* edit.* delete.* scan";
        HashMap<String, Object> body = new HashMap<>();
        body.put("grant_type", "password");
        body.put("scope", scope);
        body.put("username", username);
        body.put("password", password);
        execute(inventoryService.getToken(body), new DatabaseResponse<Token>() {
            @Override
            public void response(Token result) {
                token = result;
            }

            @Override
            public void error(DatabaseError error) {
                token = new Token("", "");
                System.err.println("Failed to login to the service: " + error.toString());
            }
        });
    }

    @Override
    public void disconnect() {
        retrofit = null;
        inventoryService = null;
        token = null;
    }

    @Override
    public void fetchInventories(DatabaseResponse<List<Inventory>> callback) {
        execute(inventoryService.listInventories(token.asAuthorization()), callback);
    }

    @Override
    public void fetchInventory(byte[] id, DatabaseResponse<Inventory> callback) {
        execute(inventoryService.fetchInventory(token.asAuthorization(), ID.toHex(id)), callback);
    }

    @Override
    public void pushInventory(byte[] id, Map<String, String> params, List<String> items, String icon, String background, DatabaseResponse<Inventory> callback) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("icon", icon);
        body.put("background", background);
        body.put("items", items);
        body.putAll(params);
        execute(inventoryService.pushInventory(token.asAuthorization(), ID.toHex(id), body), callback);
    }

    @Override
    public void createInventory(Map<String, String> params, List<String> items, String icon, String background, DatabaseResponse<Inventory> callback) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("icon", icon);
        body.put("background", background);
        body.put("items", items);
        body.putAll(params);
        execute(inventoryService.createInventory(token.asAuthorization(), body), callback);
    }

    @Override
    public void fetchItems(List<String> items, DatabaseResponse<List<Item>> callback) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("itemIds", items);
        execute(inventoryService.fetchItems(token.asAuthorization(), body), callback);
    }

    @Override
    public void fetchItem(byte[] id, DatabaseResponse<Item> callback) {
        execute(inventoryService.fetchItem(token.asAuthorization(), ID.toHex(id)), callback);
    }

    @Override
    public void createItem(Map<String, String> params, String icon, String background, DatabaseResponse<Item> callback) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("icon", icon);
        body.put("background", background);
        body.putAll(params);
        execute(inventoryService.createItem(token.asAuthorization(), body), callback);
    }

    @Override
    public void pushItem(byte[] id, Map<String, String> params, List<LocationPoint> locations, String icon, String background, DatabaseResponse<Item> callback) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("icon", icon);
        body.put("background", background);
        body.put("locations", locations);
        body.putAll(params);
        execute(inventoryService.pushItem(token.asAuthorization(), ID.toHex(id), body), callback);
    }

    @Override
    public void fetchContainers(DatabaseResponse<List<Container>> callback) {
        execute(inventoryService.listContainers(token.asAuthorization()), callback);
    }

    @Override
    public void pushContainer(byte[] id, Map<String, String> params, List<String> items, List<LocationPoint> locations, DatabaseResponse<Container> callback) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("items", items);
        body.put("locations", locations);
        body.putAll(params);
        execute(inventoryService.pushContainer(token.asAuthorization(), ID.toHex(id), body), callback);
    }

    @Override
    public void fetchContainer(byte[] id, DatabaseResponse<Container> callback) {
        execute(inventoryService.fetchContainer(token.asAuthorization(), ID.toHex(id)), callback);
    }

    @Override
    public void createContainer(Map<String, String> params, List<String> items, DatabaseResponse<Container> callback) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("items", items);
        body.putAll(params);
        execute(inventoryService.createContainer(token.asAuthorization(), body), callback);
    }

    @Override
    public void fetchScanResult(String deviceName, String objectId, String location, DatabaseResponse<HashMap<String, Object>> callback) {
        HashMap<String, String> body = new HashMap<>();
        body.put("device", deviceName);
        body.put("location", location);
        execute(inventoryService.fetchScanResult(token.asAuthorization(), objectId, body), callback);
    }

    @Override
    public void doQuery(HashMap<String, String> filters, DatabaseResponse<HashMap<String, Object>> callback) {
        execute(inventoryService.query(token.asAuthorization(), filters), callback);
    }

    private <T> void execute(Call<T> rq, DatabaseResponse<T> res){
        NetworkCall<T> call = new NetworkCall<>(rq, res);
        call.start();
    }
}
