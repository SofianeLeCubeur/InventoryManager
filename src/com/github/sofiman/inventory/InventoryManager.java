package com.github.sofiman.inventory;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.database.Database;
import com.github.sofiman.inventory.database.DatabaseError;
import com.github.sofiman.inventory.database.DatabaseResponse;
import com.github.sofiman.inventory.utils.ParametersMap;

public class InventoryManager {

    private Database database;

    public InventoryManager(Database database) {
        this.database = database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public void push(Inventory inventory){
        if(inventory.getId() == null){
            this.register(inventory);
            return;
        }
        database.pushInventory(inventory.getId(), ParametersMap.inventory(inventory), inventory.getItems(),
                inventory.getIcon(), inventory.getBackground(), new DatabaseResponse<Inventory>() {
            @Override
            public void response(Inventory callback) {

            }

            @Override
            public void error(DatabaseError error) {

            }
        });
    }

    public void push(Container container){
        if(container.getId() == null){
            this.register(container);
            return;
        }
        database.pushContainer(container.getId(), ParametersMap.container(container), container.getItems(), container.getLocations(), new DatabaseResponse<Container>() {
            @Override
            public void response(Container callback) {

            }

            @Override
            public void error(DatabaseError error) {

            }
        });
    }

    public void push(Item item){
        if(item.getId() == null){
            this.register(item);
            return;
        }
        database.pushItem(item.getId(), ParametersMap.item(item), item.getLocations(), item.getIcon(), item.getBackground(), new DatabaseResponse<Item>() {
            @Override
            public void response(Item callback) {

            }

            @Override
            public void error(DatabaseError error) {

            }
        });
    }

    public void register(Inventory inventory){
        database.createInventory(ParametersMap.inventory(inventory), inventory.getItems(), inventory.getIcon(), inventory.getBackground(), new DatabaseResponse<Inventory>() {
            @Override
            public void response(Inventory callback) {

            }

            @Override
            public void error(DatabaseError error) {

            }
        });
    }

    public void register(Container container){
        database.createContainer(ParametersMap.container(container), container.getItems(), new DatabaseResponse<Container>() {
            @Override
            public void response(Container callback) {

            }

            @Override
            public void error(DatabaseError error) {

            }
        });
    }

    public void register(Item item){
        database.createItem(ParametersMap.item(item), item.getIcon(), item.getBackground(), new DatabaseResponse<Item>() {
            @Override
            public void response(Item callback) {

            }

            @Override
            public void error(DatabaseError error) {

            }
        });
    }

}
