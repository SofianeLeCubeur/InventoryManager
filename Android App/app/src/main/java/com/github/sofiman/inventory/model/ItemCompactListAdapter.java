package com.github.sofiman.inventory.model;

import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.ui.components.ItemCompactComponent;
import com.github.sofiman.inventory.ui.components.ItemComponent;
import com.github.sofiman.inventory.utils.Callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemCompactListAdapter extends RecyclerView.Adapter<ItemCompactListAdapter.ItemCompactHolder> implements Filterable {

    private final Context context;
    private List<Item> original;
    private List<Item> items;
    private Set<Item> selected;
    private Callback<Pair<Item, ItemCompactComponent>> clickListener;

    public ItemCompactListAdapter(Context context, List<Item> items, Callback<Pair<Item, ItemCompactComponent>> clickListener) {
        this.context = context;
        this.original = items;
        this.items = new ArrayList<>(items);
        this.clickListener = clickListener;
        this.selected = new HashSet<>();
    }

    public List<Item> getItems() {
        return original;
    }

    public List<Item> getFilteredItems() {
        return items;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ItemCompactListAdapter.ItemCompactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCompactComponent component = new ItemCompactComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        component.setLayoutParams(params);
        return new ItemCompactListAdapter.ItemCompactHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemCompactListAdapter.ItemCompactHolder holder, int position) {
        ItemCompactComponent component = (ItemCompactComponent) holder.itemView;
        Item store = items.get(position);

        component.setName(store.getName());
        component.setActive(selected.contains(store));
        component.setIcon(store.getIcon());
        component.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected.contains(store)){
                    selected.remove(store);
                    component.setActive(false);
                    component.update();
                } else {
                    selected.add(store);
                    component.setActive(true);
                    component.update();
                }
                if(clickListener != null){
                    clickListener.run(Pair.create(store, component));
                }
            }
        });

        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class ItemCompactHolder extends RecyclerView.ViewHolder {
        public ItemCompactHolder(@NonNull ItemCompactComponent itemView) {
            super(itemView);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString().toLowerCase();
                List<Item> filteredItems;
                if (query.isEmpty()) {
                    filteredItems = original;
                } else {
                    filteredItems = new ArrayList<>();
                    for (Item item : original) {
                        System.out.println(item);
                        if (item.getRawId().equals(query)
                                || item.getName().toLowerCase().startsWith(query)
                                || item.getInternalName().toLowerCase().startsWith(query)
                                || item.getSerialNumber().toLowerCase().startsWith(query)) {
                            filteredItems.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredItems;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                items = (List<Item>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
