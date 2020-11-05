package com.github.sofiman.inventory.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.sofiman.inventory.ContainerActivity;
import com.github.sofiman.inventory.CreateObjectActivity;
import com.github.sofiman.inventory.InventoryActivity;
import com.github.sofiman.inventory.ItemActivity;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.ui.dialogs.DoubleEditDialog;
import com.github.sofiman.inventory.ui.login.LoginActivity;
import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Server;
import com.github.sofiman.inventory.impl.APIResponse;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.model.ContainerListAdapter;
import com.github.sofiman.inventory.model.InventoryListAdapter;
import com.github.sofiman.inventory.ui.components.ContainerComponent;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.utils.IntentBuilder;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private TextView serverName;
    private Runnable contentReset;

    // Layout
    private ShimmerFrameLayout inventoryShimmer, containerShimmer;
    private RelativeLayout inventoryLayout, containerLayout;
    private RecyclerView invRecycler, containerRecycler;
    private HorizontalScrollView actionLayout;
    private ProgressBar loadingIndicator;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(getContext() == null || getActivity() == null){
            return null;
        }

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        FrameLayout layout = root.findViewById(R.id.home_fragment_layout);
        LayoutHelper.addStatusBarOffset(layout.getContext(), layout);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        final int px = LayoutHelper.getStatusBarHeight(layout.getContext());
        params.height = params.height + px;
        layout.setLayoutParams(params);
        LinearLayout bodyLayout = root.findViewById(R.id.home_fragment_body);
        FrameLayout.LayoutParams llp = (FrameLayout.LayoutParams) bodyLayout.getLayoutParams();
        llp.setMargins(llp.leftMargin, llp.topMargin + px, llp.rightMargin, llp.bottomMargin);
        bodyLayout.setLayoutParams(llp);

        ConstraintLayout switcher = root.findViewById(R.id.home_server_switch);
        switcher.setOnClickListener(view -> openSwitchDialog());
        serverName = root.findViewById(R.id.home_server_name);
        if(Fetcher.getInstance().getCurrentServer() == null){
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return root;
        }
        serverName.setText(Fetcher.getInstance().getCurrentServer().getName());

        actionLayout = root.findViewById(R.id.home_actions_layout);
        loadingIndicator = root.findViewById(R.id.home_server_loading);

        // Inventories

        inventoryShimmer = root.findViewById(R.id.home_inv_shimmer);
        inventoryLayout = root.findViewById(R.id.home_inventory_layout);
        invRecycler = root.findViewById(R.id.home_inventories);

        inventoryShimmer.startShimmer();

        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        invRecycler.setNestedScrollingEnabled(false);
        invRecycler.setLayoutManager(llm);

        // Containers

        containerShimmer = root.findViewById(R.id.home_cnt_shimmer);
        containerLayout = root.findViewById(R.id.home_container_layout);
        containerRecycler = root.findViewById(R.id.home_containers);

        containerShimmer.startShimmer();

        LinearLayoutManager llm2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        containerRecycler.setLayoutManager(llm2);
        containerRecycler.setNestedScrollingEnabled(false);

        contentReset = new Runnable() {
            @Override
            public void run() {
                LayoutHelper.hide(inventoryLayout, containerLayout, actionLayout);
                LayoutHelper.show(inventoryShimmer, containerShimmer, loadingIndicator);
                inventoryShimmer.startShimmer();
                containerShimmer.startShimmer();
            }
        };

        root.findViewById(R.id.home_new_inv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new IntentBuilder(getActivity(), CreateObjectActivity.class)
                        .scope("create").type("inventory").blueprint(new Inventory(new byte[0], "", "", "", new ArrayList<>()))
                        .build(), 0);
            }
        });
        root.findViewById(R.id.home_new_cnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new IntentBuilder(getActivity(), CreateObjectActivity.class)
                        .scope("create").type("container").blueprint(new Container(new byte[0], "", "", "", "")
                                .setLocations(new ArrayList<>()).setItems(new ArrayList<>()))
                        .build(), 1);
            }
        });
        root.findViewById(R.id.home_new_itm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new IntentBuilder(getActivity(), CreateObjectActivity.class)
                        .scope("create").type("item")
                        .blueprint(new Item("", "", "", "", "", "", "", new ArrayList<>(), ""))
                        .build(), 2);
            }
        });

        loadData();

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(data == null) return;
        if(requestCode == 0){
            if(data.hasExtra("INVENTORY_ID")
                    && data.hasExtra("INVENTORY_NAME")
                    && data.hasExtra("INVENTORY_ICON")){
                Intent intent = new Intent(getContext(), InventoryActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        } else if(requestCode == 1){
            if(data.hasExtra("CONTAINER_ID")
                    && data.hasExtra("CONTAINER_NAME")
                    && data.hasExtra("CONTAINER_LOCATION")){
                Intent intent = new Intent(getContext(), ContainerActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        } else if(requestCode == 2){
            if(data.hasExtra("ITEM_ID")
                    && data.hasExtra("ITEM_NAME")
                    && data.hasExtra("ITEM_ICON")){
                Intent intent = new Intent(getContext(), ItemActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        }
    }

    private void openSwitchDialog() {
        final List<String> choicesString = new ArrayList<>();
        for (Pair<Server, Pair<String, String>> item : Fetcher.getInstance().getServerList()) {
            choicesString.add(item.first.getName());
        }
        String t = (String) serverName.getText();
        final AtomicInteger selectedIndex = new AtomicInteger(choicesString.indexOf(t));
        final AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.ThemeOverlay_InventoryManager_Dialog)
                .setTitle(R.string.home_switch_dialog_title)
                .setSingleChoiceItems(choicesString.toArray(new String[0]), selectedIndex.get(), (dialogInterface, i) -> selectedIndex.set(i))
                .setPositiveButton(R.string.home_switch_dialog_positive, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    changeServer(Fetcher.getInstance().getServerList().get(selectedIndex.get()));
                })
                .setNeutralButton(R.string.home_switch_dialog_neutral, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    BottomNavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                    navigationView.setSelectedItemId(R.id.navigation_settings);
                })
                .setNegativeButton(R.string.dialog_cancel, DoubleEditDialog.DISPOSE)
                .create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.colorAccent));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getColor(R.color.iconAccent));
        });
        dialog.show();
    }

    private void changeServer(Pair<Server, Pair<String, String>> server){
        contentReset.run();
        try {
            System.out.println("Trying to connect to " + server.first);
            serverName.setText(server.first.getName());
            Fetcher fetcher = Fetcher.getInstance();
            fetcher.init(server.first);
            fetcher.login(server.second.first, server.second.second, error -> {
                if(error != null){
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.login_page_no_connection, error.toString()), Toast.LENGTH_LONG).show());
                } else {
                    loadData();
                }
            });
        } catch (Exception e){
            Toast.makeText(getContext(), getString(R.string.login_page_no_connection, "(" + e.getClass().getSimpleName() + ") " + e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void loadData() {
        contentReset.run();

        Fetcher.getInstance().fetchInventories(new APIResponse<List<Inventory>>() {
            @Override
            public void response(List<Inventory> result) {
                invRecycler.setAdapter(new InventoryListAdapter(getContext(), result, data -> LayoutHelper.openInventory(getActivity(), data)));
                inventoryShimmer.stopShimmer();
                LayoutHelper.hide(inventoryShimmer, loadingIndicator);
                LayoutHelper.show(inventoryLayout, actionLayout);
            }

            @Override
            public void error(RequestError error) {
                System.err.println("Failed to fetch inventory data: " + error.toString());
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.full_page_error_inventories, error.toString()), Toast.LENGTH_LONG).show());
            }
        });
        Fetcher.getInstance().fetchContainers(new APIResponse<List<Container>>() {
            @Override
            public void response(List<Container> result) {
                containerRecycler.setAdapter(new ContainerListAdapter(getContext(), result, new Callback<Pair<Container, ContainerComponent>>() {
                    @Override
                    public void run(Pair<Container, ContainerComponent> data) {
                        LayoutHelper.openContainer(getActivity(), data);
                    }
                }));
                containerShimmer.stopShimmer();
                LayoutHelper.hide(containerShimmer, loadingIndicator);
                LayoutHelper.show(containerLayout, actionLayout);
            }

            @Override
            public void error(RequestError error) {
                System.err.println("Failed to fetch container data: " + error.toString());
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.full_page_error_containers, error.toString()), Toast.LENGTH_LONG).show());
            }
        });
    }

}