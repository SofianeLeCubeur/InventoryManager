package com.github.sofiman.inventory.model;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.SearchHistoryItem;
import com.github.sofiman.inventory.api.Tracker;
import com.github.sofiman.inventory.ui.components.SearchHistoryItemComponent;
import com.github.sofiman.inventory.ui.components.TrackerComponent;
import com.github.sofiman.inventory.utils.Callback;

import java.util.LinkedList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryItemHolder> {

    private final Context context;
    private LinkedList<SearchHistoryItem> searchHistory;
    private Callback<SearchHistoryItem> clickCallback;

    public SearchHistoryAdapter(Context context, List<SearchHistoryItem> searchHistory, Callback<SearchHistoryItem> clickCallback) {
        this.context = context;
        this.searchHistory = new LinkedList<>(searchHistory);
        this.clickCallback = clickCallback;
    }

    public List<SearchHistoryItem> getSearchHistory() {
        return searchHistory;
    }

    public void setSearchHistory(LinkedList<SearchHistoryItem> searchHistory) {
        this.searchHistory = searchHistory;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return searchHistory.size();
    }

    @NonNull
    @Override
    public SearchHistoryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SearchHistoryItemComponent component = new SearchHistoryItemComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        component.setLayoutParams(params);
        return new SearchHistoryItemHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryItemHolder holder, int position) {
        SearchHistoryItemComponent component = (SearchHistoryItemComponent) holder.itemView;
        SearchHistoryItem store = searchHistory.get(position);

        component.setQuery(store.getQuery());
        component.setType(R.drawable.magnify);
        component.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickCallback != null){
                    clickCallback.run(store);
                }
            }
        });

        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class SearchHistoryItemHolder extends RecyclerView.ViewHolder {
        public SearchHistoryItemHolder(@NonNull SearchHistoryItemComponent itemView) {
            super(itemView);
        }
    }
}
