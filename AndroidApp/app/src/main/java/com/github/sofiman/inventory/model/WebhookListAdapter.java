package com.github.sofiman.inventory.model;

import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.Webhook;
import com.github.sofiman.inventory.ui.components.WebhookComponent;

import java.util.ArrayList;
import java.util.List;

public class WebhookListAdapter extends RecyclerView.Adapter<WebhookListAdapter.WebhookHolder> implements Filterable {

    private final Context context;
    private List<Webhook> original;
    private List<Webhook> webhooks;
    private final int px;

    public WebhookListAdapter(Context context, List<Webhook> webhooks) {
        this.context = context;
        this.webhooks = webhooks;
        this.original = webhooks;
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return webhooks.size();
    }

    @NonNull
    @Override
    public WebhookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        WebhookComponent component = new WebhookComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, px);
        component.setLayoutParams(params);
        return new WebhookHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull WebhookHolder holder, int position) {
        WebhookComponent component = (WebhookComponent) holder.itemView;
        Webhook store = webhooks.get(position);

        component.setName(store.getId());
        component.setEvent(store.getEvent());
        component.setStatus(0);
        if(store.getLastDelivery() != null){
            component.setStatus(store.getLastDelivery().getStatus());
        }

        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString().toLowerCase();
                List<Webhook> filteredItems;
                if (query.isEmpty()) {
                    filteredItems = original;
                } else {
                    filteredItems = new ArrayList<>();
                    for (Webhook item : original) {
                        System.out.println(item);
                        if (item.getId().startsWith(query)
                                || item.getEvent().startsWith(query)) {
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
                webhooks = (List<Webhook>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class WebhookHolder extends RecyclerView.ViewHolder {
        public WebhookHolder(@NonNull WebhookComponent itemView) {
            super(itemView);
        }
    }
}
