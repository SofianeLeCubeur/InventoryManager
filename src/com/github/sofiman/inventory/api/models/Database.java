package com.github.sofiman.inventory.api.models;

import com.github.sofiman.inventory.api.Query;
import com.github.sofiman.inventory.impl.InventoryManager;
import org.bson.conversions.Bson;

import java.util.List;

public interface Database {

    void assignManager(InventoryManager manager);

    void update(Item item);
    void update(Container container);
    void update(Inventory inventory);

    void register(Item item);
    void register(Container container);
    void register(Inventory inventory);

    void remove(Item item);
    void remove(Container container);
    void remove(Inventory inventory);

    Item findItem(Bson filter);
    List<Item> findItems(Bson filter);
    Container findContainer(Bson filter);
    Inventory findInventory(Bson filter);

    void pushScanLog(ScanLog log);
    List<ScanLog> queryScanLog(Query filter, int offset, int length);
    List<ScanLog> queryScanLog(Query filter);

    void connect();
    void disconnect();

    /**
     * @return the period of refresh of item caches (inventories or containers) in milliseconds
     */
    long getItemCacheExpire();
    /**
     * @return if the inventory manager update the database after every change
     */
    boolean saveChanges();
}
