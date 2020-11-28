package com.github.sofiman.inventory.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.github.sofiman.inventory.ContainerActivity;
import com.github.sofiman.inventory.InventoryActivity;
import com.github.sofiman.inventory.ItemActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.ui.components.ContainerComponent;
import com.github.sofiman.inventory.ui.components.InventoryComponent;
import com.github.sofiman.inventory.ui.components.ItemComponent;

import java.util.ArrayList;
import java.util.Collection;


public class LayoutHelper {

    private static int statusBarHeight = -1;

    public static void addStatusBarOffset(Context context, View view) {
        final int statusBar = getStatusBarHeight(context);
        int left = view.getPaddingLeft(), right = view.getPaddingRight(), top = view.getPaddingTop(), bottom = view.getPaddingBottom();
        view.setPadding(left, top + statusBar, right, bottom);
    }

    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight != -1) {
            return statusBarHeight;
        }
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
            statusBarHeight = result;
        }
        return result;
    }

    public static void openInventory(Activity context, Pair<Inventory, InventoryComponent> data) {
        final byte[] id = data.first.getId();
        final InventoryComponent component = data.second;
        View image = component.findViewById(R.id.inventory_icon_text);
        if (data.first.getIcon() != null && URLUtil.isValidUrl(data.first.getIcon())) {
            image = component.findViewById(R.id.inventory_icon_image);
        }
        System.out.println(image.getTransitionName());
        final TextView name = component.findViewById(R.id.inventory_name);

        Intent intent = new Intent(context, InventoryActivity.class);
        intent.putExtra("INVENTORY_ID", id);
        intent.putExtra("INVENTORY_NAME", data.first.getName());
        intent.putExtra("INVENTORY_ICON", data.first.getIcon());
        context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context, android.util.Pair.create(image, image.getTransitionName()),
                android.util.Pair.create(name, "inventory_name")).toBundle());
    }

    public static void openContainer(Activity context, Pair<Container, ContainerComponent> data) {
        final byte[] id = data.first.getId();
        final ContainerComponent component = data.second;
        final TextView name = component.findViewById(R.id.container_name);
        final TextView location = component.findViewById(R.id.container_location);

        Intent intent = new Intent(context, ContainerActivity.class);
        intent.putExtra("CONTAINER_ID", id);
        intent.putExtra("CONTAINER_NAME", data.first.getContent());
        intent.putExtra("CONTAINER_LOCATION", data.first.getLocation());
        context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context, android.util.Pair.create(location, "container_location"),
                android.util.Pair.create(name, "container_name")).toBundle());
    }

    public static void openItem(Activity context, Pair<Item, ItemComponent> data) {
        final byte[] id = data.first.getId();
        final ItemComponent component = data.second;
        View image = component.findViewById(R.id.item_icon);
        final TextView name = component.findViewById(R.id.item_name);

        Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra("ITEM_ID", id);
        intent.putExtra("ITEM_NAME", data.first.getName());
        intent.putExtra("ITEM_ICON", data.first.getIcon());
        context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context, android.util.Pair.create(image, image.getTransitionName()),
                android.util.Pair.create(name, "item_name")).toBundle());
    }

    public static void hide(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    public static void show(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static <V extends View> Collection<V> findChildrenByClass(ViewGroup viewGroup, Class<V> clazz) {
        return gatherChildrenByClass(viewGroup, clazz, new ArrayList<V>());
    }

    public static <V extends View> Collection<V> gatherChildrenByClass(ViewGroup viewGroup, Class<V> clazz, Collection<V> childrenFound) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            final View child = viewGroup.getChildAt(i);
            if (clazz.isAssignableFrom(child.getClass())) {
                childrenFound.add((V) child);
            }
            if (child instanceof ViewGroup) {
                gatherChildrenByClass((ViewGroup) child, clazz, childrenFound);
            }
        }

        return childrenFound;
    }

}
