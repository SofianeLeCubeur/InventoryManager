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

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.ui.components.ContainerComponent;
import com.github.sofiman.inventory.utils.Callback;

import java.util.List;

public class ContainerListAdapter extends RecyclerView.Adapter<ContainerListAdapter.ContainerHolder> {

    private final Context context;
    private List<Container> containers;
    private Callback<Pair<Container, ContainerComponent>> callback;
    private final int px;

    public ContainerListAdapter(Context context, List<Container> inventories, Callback<Pair<Container, ContainerComponent>> callback) {
        this.context = context;
        this.containers = inventories;
        this.callback = callback;
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
    }

    public List<Container> getContainers() {
        return containers;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return containers.size();
    }

    @NonNull
    @Override
    public ContainerListAdapter.ContainerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContainerComponent component = new ContainerComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, px);
        component.setLayoutParams(params);
        return new ContainerListAdapter.ContainerHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull ContainerListAdapter.ContainerHolder holder, int position) {
        ContainerComponent component = (ContainerComponent) holder.itemView;
        Container store = containers.get(position);

        component.setContent(store.getContent());
        component.setLocation(store.getLocation());
        component.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null){
                    callback.run(new Pair<>(store, component));
                }
            }
        });

        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class ContainerHolder extends RecyclerView.ViewHolder {
        public ContainerHolder(@NonNull ContainerComponent itemView) {
            super(itemView);
        }
    }
}
