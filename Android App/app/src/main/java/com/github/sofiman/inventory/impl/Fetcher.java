package com.github.sofiman.inventory.impl;

import android.os.AsyncTask;
import android.util.Pair;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.DataField;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.api.Token;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.api.ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
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

    public void init(Server server) {
        this.retrofit = new Retrofit.Builder()
                .baseUrl(server.getEndpoint())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.accessToken = null;
        this.inventoryService = retrofit.create(InventoryService.class);
        this.currentServer = server;
    }

    public void login(String username, String password, Callback<RequestError> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<Token> networkCall = new NetworkCall<Token>(new APIResponse<Token>() {
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
                final String scope = "fetch.* scan add.inv add.* edit.* delete.*";
                HashMap<String, Object> body = new HashMap<>();
                body.put("grant_type", "password");
                body.put("scope", scope);
                body.put("username", username);
                body.put("password", password);
                networkCall.execute(inventoryService.getToken(body));
            }
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
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<List<Inventory>> networkCall = new NetworkCall<>(callback);
                networkCall.execute(inventoryService.listInventories(accessToken.asAuthorization()));
            }
        });
    }

    public void fetchInventory(final byte[] id, final APIResponse<Inventory> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<Inventory> networkCall = new NetworkCall<>(callback);
                networkCall.execute(inventoryService.fetchInventory(accessToken.asAuthorization(), ID.toHex(id)));
            }
        });
    }

    public void pushInventory(final byte[] id, List<DataField> dataFields, List<String> items, String icon, String background, final APIResponse<Inventory> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<Inventory> networkCall = new NetworkCall<>(callback);
                HashMap<String, Object> body = new HashMap<>();
                body.put("icon", icon);
                body.put("background", background);
                body.put("items", items);
                for (DataField field : dataFields) {
                    body.put(field.getId(), field.getValue());
                }
                networkCall.execute(inventoryService.pushInventory(accessToken.asAuthorization(), ID.toHex(id), body));
            }
        });
    }

    public void createInventory(List<DataField> dataFields, List<String> items, String icon, String background, final APIResponse<Inventory> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<Inventory> networkCall = new NetworkCall<>(callback);
                HashMap<String, Object> body = new HashMap<>();
                body.put("icon", icon);
                body.put("background", background);
                body.put("items", items);
                for (DataField field : dataFields) {
                    body.put(field.getId(), field.getValue());
                }
                networkCall.execute(inventoryService.createInventory(accessToken.asAuthorization(), body));
            }
        });
    }

    public void fetchItems(List<String> items, final APIResponse<List<Item>> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<List<Item>> networkCall = new NetworkCall<>(callback);
                HashMap<String, Object> body = new HashMap<>();
                body.put("itemIds", items);
                networkCall.execute(inventoryService.fetchItems(accessToken.asAuthorization(), body));
            }
        });
    }

    public void fetchItem(byte[] itemId, final APIResponse<Item> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<Item> networkCall = new NetworkCall<>(callback);
                networkCall.execute(inventoryService.fetchItem(accessToken.asAuthorization(), ID.toHex(itemId)));
            }
        });
    }

    public void pushItem(final byte[] id, List<DataField> dataFields, String icon, String background, final APIResponse<Item> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<Item> networkCall = new NetworkCall<>(callback);
                HashMap<String, Object> body = new HashMap<>();
                body.put("icon", icon);
                body.put("background", background);
                for (DataField field : dataFields) {
                    body.put(field.getId(), field.getValue());
                }
                networkCall.execute(inventoryService.pushItem(accessToken.asAuthorization(), ID.toHex(id), body));
            }
        });
    }

    public void fetchContainers(final APIResponse<List<Container>> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<List<Container>> networkCall = new NetworkCall<>(callback);
                networkCall.execute(inventoryService.listContainers(accessToken.asAuthorization()));
            }
        });
    }

    public void pushContainer(final byte[] id, List<DataField> dataFields, List<String> items, String icon, String background, final APIResponse<Container> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<Container> networkCall = new NetworkCall<>(callback);
                HashMap<String, Object> body = new HashMap<>();
                body.put("icon", icon);
                body.put("background", background);
                body.put("items", items);
                for (DataField field : dataFields) {
                    body.put(field.getId(), field.getValue());
                }
                networkCall.execute(inventoryService.pushContainer(accessToken.asAuthorization(), ID.toHex(id), body));
            }
        });
    }

    public void fetchContainer(byte[] containerId, final APIResponse<Container> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<Container> networkCall = new NetworkCall<>(callback);
                networkCall.execute(inventoryService.fetchContainer(accessToken.asAuthorization(), ID.toHex(containerId)));
            }
        });
    }

    public void fetchScanResult(String objectId, String location, final APIResponse<HashMap<String, Object>> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<HashMap<String, Object>> networkCall = new NetworkCall<>(callback);
                HashMap<String, String> body = new HashMap<>();
                body.put("device", HistoryDataModel.getDeviceName());
                body.put("location", location);
                networkCall.execute(inventoryService.fetchScanResult(accessToken.asAuthorization(), objectId, body));
            }
        });
    }

    public void doQuery(HashMap<String, String> filters, final APIResponse<HashMap<String, Object>> callback) {
        safeExecute(new Runnable() {
            @Override
            public void run() {
                final NetworkCall<HashMap<String, Object>> networkCall = new NetworkCall<>(callback);
                networkCall.execute(inventoryService.query(accessToken.asAuthorization(), filters));
            }
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
