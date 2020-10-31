package com.github.sofiman.inventory.model;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.ui.components.ContainerComponent;
import com.github.sofiman.inventory.ui.components.SampleLinkComponent;
import com.github.sofiman.inventory.utils.Callback;

import java.util.List;

public class SampleListAdapter extends RecyclerView.Adapter<SampleListAdapter.SampleLinkHolder> {

    private final Context context;
    private List<String> links;
    private Callback<String> onClickListener;
    private final int px;

    public SampleListAdapter(Context context, List<String> links) {
        this.context = context;
        this.links = links;
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
    }

    public List<String> getLinks() {
        return links;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return links.size();
    }

    public void setOnClickListener(Callback<String> onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public SampleListAdapter.SampleLinkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SampleLinkComponent component = new SampleLinkComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, px);
        component.setLayoutParams(params);
        return new SampleListAdapter.SampleLinkHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleListAdapter.SampleLinkHolder holder, int position) {
        SampleLinkComponent component = (SampleLinkComponent) holder.itemView;
        String store = links.get(position);

        component.setLabel(store);

        component.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onClickListener != null){
                    onClickListener.run(store);
                }
            }
        });
        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class SampleLinkHolder extends RecyclerView.ViewHolder {
        public SampleLinkHolder(@NonNull SampleLinkComponent itemView) {
            super(itemView);
        }
    }
}
