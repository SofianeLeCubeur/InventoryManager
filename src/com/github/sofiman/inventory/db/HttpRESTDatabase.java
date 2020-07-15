package com.github.sofiman.inventory.db;

import com.github.sofiman.inventory.api.Query;
import com.github.sofiman.inventory.api.models.*;
import com.github.sofiman.inventory.impl.InventoryManager;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * TODO: This calls is not finished because the REST API Server is not ready
 */
public class HttpRESTDatabase implements Database {

    private InventoryManager manager;
    private String endpoint;
    private String id, secret, grantType;
    private String accessToken, refreshToken;

    private final long itemCacheExpire;
    private final boolean saveChanges;

    public HttpRESTDatabase(String endpoint, String id, String secret, String grantType) {
        this(endpoint, id, secret, grantType, 3 * 60 * 1000, false);
    }

    public HttpRESTDatabase(String endpoint, String id, String secret, String grantType, long itemCacheExpire, boolean saveChanges) {
        this.endpoint = endpoint;
        this.id = id;
        this.secret = secret;
        this.grantType = grantType;
        this.itemCacheExpire = itemCacheExpire;
        this.saveChanges = saveChanges;
    }

    @Override
    public void assignManager(InventoryManager manager) {
        this.manager = manager;
    }

    @Override
    public void update(Item item) {

    }

    @Override
    public void update(Container container) {

    }

    @Override
    public void update(Inventory inventory) {

    }

    @Override
    public void register(Item item) {

    }

    @Override
    public void register(Container container) {

    }

    @Override
    public void register(Inventory inventory) {

    }

    @Override
    public void remove(Item item) {

    }

    @Override
    public void remove(Container container) {

    }

    @Override
    public void remove(Inventory inventory) {

    }

    @Override
    public Item findItem(Bson filter) {
        return null;
    }

    @Override
    public List<Item> findItems(Bson filter) {
        return null;
    }

    @Override
    public Container findContainer(Bson filter) {
        return null;
    }

    @Override
    public Inventory findInventory(Bson filter) {
        return null;
    }

    @Override
    public void pushScanLog(ScanLog log) {

    }

    @Override
    public List<ScanLog> queryScanLog(Query filter, int offset, int length) {
        return null;
    }

    @Override
    public List<ScanLog> queryScanLog(Query filter) {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public long getItemCacheExpire() {
        return 0;
    }

    @Override
    public boolean saveChanges() {
        return false;
    }

}
