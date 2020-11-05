package com.github.sofiman.inventory.utils;

import android.content.Context;
import android.content.Intent;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.DataField;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntentBuilder {

    private final Intent intent;

    public IntentBuilder(Context context, Class<?> clazz){
        this.intent = new Intent(context, clazz);
    }

    public IntentBuilder(){
        this.intent = new Intent();
    }

    public IntentBuilder icon(String icon){
        this.intent.putExtra("icon", icon);

        return this;
    }

    public IntentBuilder background(String background){
        this.intent.putExtra("background", background);

        return this;
    }

    public IntentBuilder scope(String scope){
        this.intent.putExtra("scope", scope);

        return this;
    }

    public IntentBuilder type(String type){
        this.intent.putExtra("type", type);

        return this;
    }

    public IntentBuilder extra(List<String> extra, int idx){
        this.intent.putStringArrayListExtra("extra", new ArrayList<>(extra));
        this.intent.putExtra("extraIdx", idx);

        return this;
    }

    public IntentBuilder blueprint(Inventory inventory){
        intent.putParcelableArrayListExtra("fields", new ArrayList<>(Arrays.asList(
                new DataField(0, inventory.getName(), "Inventory Name").id("name"),
                new DataField(R.drawable.icon_ionic_ios_pin, inventory.getLocation(), "Location").id("location"),
                new DataField(R.drawable.state_machine, inventory.getState(), "Inventory State").id("state")
        )));
        intent.putStringArrayListExtra("items", new ArrayList<>(inventory.getItems()));
        intent.putExtra("object_id", inventory.getId());

        return this;
    }

    public IntentBuilder blueprint(Container container){
        intent.putParcelableArrayListExtra("fields", new ArrayList<>(Arrays.asList(
                new DataField(0, container.getContent(), "Container Content or Name").id("content"),
                new DataField(R.drawable.state_machine, container.getState(), "Container State").id("state"),
                new DataField(R.drawable.icon_awesome_info, container.getDetails(), "Container Details").id("details")
        )));
        intent.putParcelableArrayListExtra("locations", new ArrayList<>(container.getLocations()));
        intent.putStringArrayListExtra("items", new ArrayList<>(container.getItems()));
        intent.putExtra("object_id", container.getId());

        return this;
    }

    public IntentBuilder blueprint(Item item){
        intent.putParcelableArrayListExtra("fields", new ArrayList<>(Arrays.asList(
                new DataField(0, item.getName(), "Item Name").id("name"),
                new DataField(R.drawable.icon_material_book, item.getInternalName(), "Item Internal Name").id("reference"),
                new DataField(R.drawable.id_card, item.getSerialNumber(), "Item Serial Number").id("serial_number"),
                new DataField(R.drawable.icon_ionic_md_list, item.getDescription(), "Item Description").id("description"),
                new DataField(R.drawable.icon_awesome_info, item.getDetails(), "Item Details").id("details"),
                new DataField(R.drawable.state_machine, item.getState(), "Item State").id("details")
        )));
        intent.putParcelableArrayListExtra("locations", new ArrayList<>(item.getLocations()));
        intent.putExtra("object_id", item.getId());

        return this;
    }

    public IntentBuilder item(Item callback) {
        intent.putExtra("ITEM_ID", callback.getId());
        intent.putExtra("ITEM_NAME", callback.getName());
        intent.putExtra("ITEM_ICON", callback.getIcon());

        return this;
    }

    public IntentBuilder inventory(Inventory callback) {
        intent.putExtra("INVENTORY_ID", callback.getId());
        intent.putExtra("INVENTORY_NAME", callback.getName());
        intent.putExtra("INVENTORY_ICON", callback.getIcon());
        return this;
    }

    public IntentBuilder container(Container callback) {
        intent.putExtra("CONTAINER_ID", callback.getRawId());
        intent.putExtra("CONTAINER_NAME", callback.getContent());
        intent.putExtra("CONTAINER_LOCATION", callback.getLocation());
        return this;
    }

    public Intent build(){
        return this.intent;
    }
}
