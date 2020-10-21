package com.github.sofiman.inventory.model;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.ui.components.LocationPointComponent;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.StringUtils;

import java.util.List;

public class LocationPointAdapter extends RecyclerView.Adapter<LocationPointAdapter.LocationPointHolder> {

    private final Activity context;
    private List<LocationPoint> locations;
    private Callback<LocationPoint> onClick;

    public LocationPointAdapter(Activity context, List<LocationPoint> locations) {
        this.context = context;
        this.locations = locations;
    }

    public List<LocationPoint> getLocations() {
        return locations;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void setOnClick(Callback<LocationPoint> onClick) {
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public LocationPointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LocationPointComponent component = new LocationPointComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        component.setLayoutParams(params);
        return new LocationPointHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationPointHolder holder, int position) {
        LocationPointComponent component = (LocationPointComponent) holder.itemView;
        LocationPoint store = locations.get(position);

        component.resetContinuity();


        if(position == 0){
            component.setFirstInContinuity();
        }
        if(position == locations.size()-1){
            component.setLastInContinuity();
        }

        if(store.getTimestamp() == -1){
            component.setAsCreate();
            component.setTimestamp(context.getString(R.string.create_object_update_location_details));
            component.setLocation(store.getLocation());
        } else {
            component.resetCreate();
            component.setTimestamp(StringUtils.formatDate(context, store.getTimestamp()));
            component.setLocation(store.getLocation());
        }
        component.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClick != null){
                    onClick.run(store);
                }
            }
        });

        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class LocationPointHolder extends RecyclerView.ViewHolder {
        public LocationPointHolder(@NonNull LocationPointComponent scanResultComponent) {
            super(scanResultComponent);
        }
    }
}
