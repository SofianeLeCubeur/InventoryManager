package com.github.sofiman.inventory.model;

import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.ui.components.InventoryComponent;
import com.github.sofiman.inventory.utils.Callback;

import java.util.List;

public class InventoryListAdapter extends RecyclerView.Adapter<InventoryListAdapter.InventoryHolder> {

    private final Context context;
    private List<Inventory> inventories;
    private Callback<Pair<Inventory, InventoryComponent>> clickCallback;
    private final int px;

    public InventoryListAdapter(Context context, List<Inventory> inventories, Callback<Pair<Inventory, InventoryComponent>> clickCallback) {
        this.context = context;
        this.inventories = inventories;
        this.clickCallback = clickCallback;
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
    }

    public List<Inventory> getInventories() {
        return inventories;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return inventories.size();
    }

    @NonNull
    @Override
    public InventoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        InventoryComponent component = new InventoryComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, px);
        component.setLayoutParams(params);
        return new InventoryHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryHolder holder, int position) {
        InventoryComponent component = (InventoryComponent) holder.itemView;
        final Inventory store = inventories.get(position);

        component.setName(store.getName());
        component.setIcon(store.getIcon());
        component.setItemCount(store.getItemCount());
        component.setTrackerCount(store.getTrackerCount());
        component.setLocation(store.getLocation());

        component.update();
        component.setOnClickListener(view -> {
            if(clickCallback != null){
                clickCallback.run(new Pair<>(store, component));
            }
        });
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class InventoryHolder extends RecyclerView.ViewHolder {
        public InventoryHolder(@NonNull InventoryComponent itemView) {
            super(itemView);
        }
    }
}
