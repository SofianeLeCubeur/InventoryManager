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
import com.github.sofiman.inventory.ui.components.ItemComponent;
import com.github.sofiman.inventory.utils.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemHolder> implements Filterable {

    private final Context context;
    private List<Item> original;
    private List<Item> items;
    private Callback<Pair<Item, ItemComponent>> clickListener;
    private final int px;

    public ItemListAdapter(Context context, List<Item> items, Callback<Pair<Item, ItemComponent>> clickListener) {
        this.context = context;
        this.original = items;
        this.items = new ArrayList<>(items);
        this.clickListener = clickListener;
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
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
    public ItemListAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemComponent component = new ItemComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, px);
        component.setLayoutParams(params);
        return new ItemListAdapter.ItemHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemListAdapter.ItemHolder holder, int position) {
        ItemComponent component = (ItemComponent) holder.itemView;
        Item store = items.get(position);

        component.setName(store.getName());
        component.setIcon(store.getIcon());
        component.setDescription(store.getDescription());
        component.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    public void update(){
        this.items = original;
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        public ItemHolder(@NonNull ItemComponent itemView) {
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
