package com.github.sofiman.inventory.utils;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;

import java.util.HashMap;
import java.util.Map;

public class ParametersMap {

    public static Map<String, String> inventory(Inventory inventory){
        Map<String, String> params = new HashMap<>();
        params.put("name", inventory.getName());
        params.put("location", inventory.getLocation());
        params.put("state", inventory.getState());
        return params;
    }

    public static Map<String, String> container(Container container){
        Map<String, String> params = new HashMap<>();
        params.put("content", container.getContent());
        params.put("state", container.getState());
        params.put("details", container.getDetails());
        return params;
    }

    public static Map<String, String> item(Item item){
        Map<String, String> params = new HashMap<>();
        params.put("name", item.getName());
        params.put("reference", item.getInternalName());
        params.put("serial_number", item.getSerialNumber());
        params.put("description", item.getDescription());
        params.put("details", item.getDetails());
        params.put("state", item.getState());
        return params;
    }
}
