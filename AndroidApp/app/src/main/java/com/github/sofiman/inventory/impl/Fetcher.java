package com.github.sofiman.inventory.impl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Pair;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.DataField;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.api.Token;
import com.github.sofiman.inventory.api.User;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.api.ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Fetcher {

    private static final Fetcher instance = new Fetcher();

    public static Fetcher getInstance() {
        return instance;
    }

    private Retrofit retrofit;
    private List<Pair<Server, Pair<String, String>>> serverList = new ArrayList<>();

    private InventoryService inventoryService;
    private Token accessToken;
    private Server currentServer;

    public void init(Context context, Server server) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo == null ? "v1" : pInfo.versionName;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(server.getEndpoint())
                .client(new OkHttpClient.Builder().addInterceptor(new UserAgentInterceptor("InventoryManager", version)).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.accessToken = null;
        this.inventoryService = retrofit.create(InventoryService.class);
        this.currentServer = server;
    }

    public void register(String username, String password, Callback<RequestError> callback){
        safeExecute(() -> {
            final NetworkCall<User> networkCall = new NetworkCall<>(new APIResponse<User>() {
                @Override
                public void response(User user) {
                    System.out.println("Successfully created account " + user.getUsername() + " with id " + user.getId());
                    login(username, password, callback);
                }

                @Override
                public void error(RequestError error) {
                    System.err.println("Failed to create an account: " + error.toString());
                    callback.run(error);
                }
            });
            HashMap<String, Object> body = new HashMap<>();
            body.put("username", username);
            body.put("password", password);
            networkCall.execute(inventoryService.register(body));
        });
    }

    public void login(String username, String password, Callback<RequestError> callback) {
        safeExecute(() -> {
            final NetworkCall<Token> networkCall = new NetworkCall<>(new APIResponse<Token>() {
                @Override
                public void response(Token result) {
                    accessToken = result;
                    callback.run(null);
                }

                @Override
                public void error(RequestError error) {
                    accessToken = new Token("", "");
                    System.err.println("Failed to login to the service: " + error.toString());
                    callback.run(error);
                }
            });
            final String scope = "fetch.* add.* edit.* delete.* scan";
            HashMap<String, Object> body = new HashMap<>();
            body.put("grant_type", "password");
            body.put("scope", scope);
            body.put("username", username);
            body.put("password", password);
            networkCall.execute(inventoryService.getToken(body));
        });
    }

    public void setServerList(List<Pair<Server, Pair<String, String>>> serverList) {
        this.serverList = serverList;
    }

    public Server getCurrentServer() {
        return currentServer;
    }

    public List<Pair<Server, Pair<String, String>>> getServerList() {
        return serverList;
    }

    public void fetchInventories(final APIResponse<List<Inventory>> callback) {
        safeExecute(() -> {
            final NetworkCall<List<Inventory>> networkCall = new NetworkCall<>(callback);
            networkCall.execute(inventoryService.listInventories(accessToken.asAuthorization()));
        });
    }

    public void fetchInventory(final byte[] id, final APIResponse<Inventory> callback) {
        safeExecute(() -> {
            final NetworkCall<Inventory> networkCall = new NetworkCall<>(callback);
            networkCall.execute(inventoryService.fetchInventory(accessToken.asAuthorization(), ID.toHex(id)));
        });
    }

    public void pushInventory(final byte[] id, List<DataField> dataFields, List<String> items, String icon, String background, final APIResponse<Inventory> callback) {
        safeExecute(() -> {
            final NetworkCall<Inventory> networkCall = new NetworkCall<>(callback);
            HashMap<String, Object> body = new HashMap<>();
            body.put("icon", icon);
            body.put("background", background);
            body.put("items", items);
            for (DataField field : dataFields) {
                body.put(field.getId(), field.getValue());
            }
            networkCall.execute(inventoryService.pushInventory(accessToken.asAuthorization(), ID.toHex(id), body));
        });
    }

    public void createInventory(List<DataField> dataFields, List<String> items, String icon, String background, final APIResponse<Inventory> callback) {
        safeExecute(() -> {
            final NetworkCall<Inventory> networkCall = new NetworkCall<>(callback);
            HashMap<String, Object> body = new HashMap<>();
            body.put("icon", icon);
            body.put("background", background);
            body.put("items", items);
            for (DataField field : dataFields) {
                body.put(field.getId(), field.getValue());
            }
            networkCall.execute(inventoryService.createInventory(accessToken.asAuthorization(), body));
        });
    }

    public void fetchItems(List<String> items, final APIResponse<List<Item>> callback) {
        safeExecute(() -> {
            final NetworkCall<List<Item>> networkCall = new NetworkCall<>(callback);
            HashMap<String, Object> body = new HashMap<>();
            body.put("itemIds", items);
            networkCall.execute(inventoryService.fetchItems(accessToken.asAuthorization(), body));
        });
    }

    public void fetchItem(byte[] itemId, final APIResponse<Item> callback) {
        safeExecute(() -> {
            final NetworkCall<Item> networkCall = new NetworkCall<>(callback);
            networkCall.execute(inventoryService.fetchItem(accessToken.asAuthorization(), ID.toHex(itemId)));
        });
    }

    public void createItem(List<DataField> dataFields, String icon, String background, final APIResponse<Item> callback) {
        safeExecute(() -> {
            final NetworkCall<Item> networkCall = new NetworkCall<>(callback);
            HashMap<String, Object> body = new HashMap<>();
            body.put("icon", icon);
            body.put("background", background);
            for (DataField field : dataFields) {
                body.put(field.getId(), field.getValue());
            }
            networkCall.execute(inventoryService.createItem(accessToken.asAuthorization(), body));
        });
    }

    public void pushItem(final byte[] id, List<DataField> dataFields, List<LocationPoint> locations, String icon, String background, final APIResponse<Item> callback) {
        safeExecute(() -> {
            final NetworkCall<Item> networkCall = new NetworkCall<>(callback);
            HashMap<String, Object> body = new HashMap<>();
            body.put("icon", icon);
            body.put("background", background);
            body.put("locations", locations);
            for (DataField field : dataFields) {
                body.put(field.getId(), field.getValue());
            }
            networkCall.execute(inventoryService.pushItem(accessToken.asAuthorization(), ID.toHex(id), body));
        });
    }

    public void fetchContainers(final APIResponse<List<Container>> callback) {
        safeExecute(() -> {
            final NetworkCall<List<Container>> networkCall = new NetworkCall<>(callback);
            networkCall.execute(inventoryService.listContainers(accessToken.asAuthorization()));
        });
    }

    public void pushContainer(final byte[] id, List<DataField> dataFields, List<String> items, List<LocationPoint> locations, String icon, String background, final APIResponse<Container> callback) {
        safeExecute(() -> {
            final NetworkCall<Container> networkCall = new NetworkCall<>(callback);
            HashMap<String, Object> body = new HashMap<>();
            body.put("icon", icon);
            body.put("background", background);
            body.put("items", items);
            body.put("locations", locations);
            for (DataField field : dataFields) {
                body.put(field.getId(), field.getValue());
            }
            networkCall.execute(inventoryService.pushContainer(accessToken.asAuthorization(), ID.toHex(id), body));
        });
    }

    public void fetchContainer(byte[] containerId, final APIResponse<Container> callback) {
        safeExecute(() -> {
            final NetworkCall<Container> networkCall = new NetworkCall<>(callback);
            networkCall.execute(inventoryService.fetchContainer(accessToken.asAuthorization(), ID.toHex(containerId)));
        });
    }

    public void createContainer(List<DataField> dataFields, List<String> items, final APIResponse<Container> callback) {
        safeExecute(() -> {
            final NetworkCall<Container> networkCall = new NetworkCall<>(callback);
            HashMap<String, Object> body = new HashMap<>();
            body.put("items", items);
            for (DataField field : dataFields) {
                body.put(field.getId(), field.getValue());
            }
            networkCall.execute(inventoryService.createContainer(accessToken.asAuthorization(), body));
        });
    }

    public void fetchScanResult(String objectId, String location, final APIResponse<HashMap<String, Object>> callback) {
        safeExecute(() -> {
            final NetworkCall<HashMap<String, Object>> networkCall = new NetworkCall<>(callback);
            HashMap<String, String> body = new HashMap<>();
            body.put("device", HistoryDataModel.getDeviceName());
            body.put("location", location);
            networkCall.execute(inventoryService.fetchScanResult(accessToken.asAuthorization(), objectId, body));
        });
    }

    public void doQuery(HashMap<String, String> filters, final APIResponse<HashMap<String, Object>> callback) {
        safeExecute(() -> {
            final NetworkCall<HashMap<String, Object>> networkCall = new NetworkCall<>(callback);
            networkCall.execute(inventoryService.query(accessToken.asAuthorization(), filters));
        });
    }

    public void safeExecute(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
