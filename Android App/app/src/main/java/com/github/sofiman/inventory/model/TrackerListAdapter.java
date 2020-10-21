package com.github.sofiman.inventory.model;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Tracker;
import com.github.sofiman.inventory.ui.components.ContainerComponent;
import com.github.sofiman.inventory.ui.components.TrackerComponent;

import java.util.List;

public class TrackerListAdapter extends RecyclerView.Adapter<TrackerListAdapter.TrackerHolder> {

    private final Context context;
    private List<Tracker> trackers;
    private final int px;

    public TrackerListAdapter(Context context, List<Tracker> trackers) {
        this.context = context;
        this.trackers = trackers;
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
    }

    public List<Tracker> getTrackers() {
        return trackers;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return trackers.size();
    }

    @NonNull
    @Override
    public TrackerListAdapter.TrackerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TrackerComponent component = new TrackerComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, px);
        component.setLayoutParams(params);
        return new TrackerListAdapter.TrackerHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackerListAdapter.TrackerHolder holder, int position) {
        TrackerComponent component = (TrackerComponent) holder.itemView;
        Tracker store = trackers.get(position);

        component.setName(store.getName());
        component.setLocation(store.getLocation());
        component.setIcon(store.getIcon());

        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class TrackerHolder extends RecyclerView.ViewHolder {
        public TrackerHolder(@NonNull TrackerComponent itemView) {
            super(itemView);
        }
    }
}
