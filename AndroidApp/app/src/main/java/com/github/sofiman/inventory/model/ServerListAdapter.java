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
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.ui.components.ContainerComponent;
import com.github.sofiman.inventory.ui.components.ServerComponent;
import com.github.sofiman.inventory.utils.Callback;

import java.util.List;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.ServerHolder> {

    private final Context context;
    private List<Pair<Server, Pair<String, String>>> servers;
    private Callback<Pair<Server, Pair<String, String>>> editCallback, deleteCallback, favoriteCallback;
    private final int px;

    public ServerListAdapter(Context context, List<Pair<Server, Pair<String, String>>> servers) {
        this.context = context;
        this.servers = servers;
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
    }

    public ServerListAdapter setListeners(Callback<Pair<Server, Pair<String, String>>> editCallback,
                                          Callback<Pair<Server, Pair<String, String>>> deleteCallback,
                                          Callback<Pair<Server, Pair<String, String>>> favoriteCallback) {
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
        this.favoriteCallback = favoriteCallback;

        return this;
    }

    public List<Pair<Server, Pair<String, String>>> getServers() {
        return servers;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    @NonNull
    @Override
    public ServerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ServerComponent component = new ServerComponent(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, px);
        component.setLayoutParams(params);
        return new ServerHolder(component);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerHolder holder, int position) {
        ServerComponent component = (ServerComponent) holder.itemView;
        Pair<Server, Pair<String, String>> store = servers.get(position);
        Server server = store.first;

        component.setName(server.getName());
        component.setIp(server.getEndpoint());
        component.setChecked(server.isDefaultServer());
        component.setEditListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editCallback != null){
                    editCallback.run(store);
                }
            }
        });
        component.setDeleteListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deleteCallback != null){
                    deleteCallback.run(store);
                }
            }
        });
        component.setCheckedListener(new Callback<Boolean>() {
            @Override
            public void run(Boolean data) {
                for (Pair<Server, Pair<String, String>> s : servers){
                    s.first.setAsDefaultServer(false);
                }
                server.setAsDefaultServer(data);
                favoriteCallback.run(store);
                notifyDataSetChanged();
            }
        });

        component.update();
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    public static class ServerHolder extends RecyclerView.ViewHolder {
        public ServerHolder(@NonNull ServerComponent serverComponent) {
            super(serverComponent);
        }
    }
}
