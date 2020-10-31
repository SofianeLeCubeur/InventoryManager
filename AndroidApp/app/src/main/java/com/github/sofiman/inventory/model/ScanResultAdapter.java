package com.github.sofiman.inventory.model;

import android.app.Activity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.ScanLog;
import com.github.sofiman.inventory.ui.components.Component;
import com.github.sofiman.inventory.ui.components.ContainerComponent;
import com.github.sofiman.inventory.ui.components.InventoryComponent;
import com.github.sofiman.inventory.ui.components.ItemComponent;
import com.github.sofiman.inventory.ui.components.ScanResultComponent;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.github.sofiman.inventory.utils.StringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ScanResultHolder> {

    private final Activity context;
    private LinkedList<ScanLog> history;
    private Set<ScanLog> selected;
    private Runnable onSelectListener;

    public ScanResultAdapter(Activity context, LinkedList<ScanLog> history) {
        this.context = context;
        this.history = history;
        this.selected = new HashSet<>();
    }

    public LinkedList<ScanLog> getHistory() {
        return history;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public void setOnSelectListener(Runnable onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    public Set<ScanLog> getSelected() {
        return selected;
    }

    @NonNull
    @Override
    public ScanResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ScanResultComponent component = new ScanResultComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        component.setLayoutParams(params);
        return new ScanResultHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanResultHolder holder, int position) {
        ScanResultComponent component = (ScanResultComponent) holder.itemView;
        ScanLog store = history.get(position);

        component.setContent(store.getContent());

        component.setTimestamp(StringUtils.formatDate(context, store.getTimestamp()));

        Component v = null;
        switch (store.getType()){
            case "inventory":
                Inventory s = (Inventory) store.getObject();
                InventoryComponent cp = new InventoryComponent(context);
                cp.setName(s.getName());
                cp.setIcon(s.getIcon());
                cp.setItemCount(s.getItemCount());
                cp.setTrackerCount(s.getTrackerCount());
                cp.setLocation(s.getLocation());
                cp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutHelper.openInventory(context, Pair.create(s, cp));
                    }
                });
                component.setType(R.drawable.icon_awesome_warehouse);

                v = cp;
                break;
            case "item":
                Item i = (Item) store.getObject();
                ItemComponent pp = new ItemComponent(context);
                pp.setName(i.getName());
                pp.setIcon(i.getIcon());
                pp.setDescription(i.getDescription());
                pp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutHelper.openItem(context, Pair.create(i, pp));
                    }
                });
                component.setType(R.drawable.cube);

                v = pp;
                break;
            case "container":
                Container c = (Container) store.getObject();
                ContainerComponent cc = new ContainerComponent(context);
                cc.setContent(c.getContent());
                cc.setLocation(c.getLocation());
                cc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutHelper.openContainer(context, Pair.create(c, cc));
                    }
                });
                component.setType(R.drawable.bag_personal);

                v = cc;
                break;
            default:
                component.setType(R.drawable.icon_awesome_question);
                break;
        }

        if(v != null){
            v.update();
        }
        component.setBody(v);
        component.setChecked(selected.contains(store));
        component.setOnCheckedListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    selected.add(store);
                } else {
                    selected.remove(store);
                }
                onSelectListener.run();
            }
        });
        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class ScanResultHolder extends RecyclerView.ViewHolder {
        public ScanResultHolder(@NonNull ScanResultComponent scanResultComponent) {
            super(scanResultComponent);
        }
    }
}
