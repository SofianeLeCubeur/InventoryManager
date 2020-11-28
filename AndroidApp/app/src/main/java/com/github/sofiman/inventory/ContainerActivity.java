package com.github.sofiman.inventory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.DataField;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.impl.APIResponse;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.model.ItemListAdapter;
import com.github.sofiman.inventory.model.LocationPointAdapter;
import com.github.sofiman.inventory.ui.components.ItemComponent;
import com.github.sofiman.inventory.ui.dialogs.QrCodeGeneratorDialog;
import com.github.sofiman.inventory.utils.Callback;
import com.github.sofiman.inventory.api.ID;
import com.github.sofiman.inventory.utils.IntentBuilder;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.github.sofiman.inventory.utils.StringUtils;
import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContainerActivity extends AppCompatActivity {

    private Container container;

    private ShimmerFrameLayout itemShimmer;
    private RelativeLayout itemsLayout;
    private  ShimmerFrameLayout stateShimmer;
    private ShimmerFrameLayout detailsShimmer;

    private AtomicBoolean valid = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        final String containerName = getIntent().getStringExtra("CONTAINER_NAME");
        final String containerLocation = getIntent().getStringExtra("CONTAINER_LOCATION");
        final byte[] containerId = getIntent().getByteArrayExtra("CONTAINER_ID");

        if(containerId == null){
            supportFinishAfterTransition();
            return;
        }

        ImageView back = findViewById(R.id.container_full_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valid.set(false);
                supportFinishAfterTransition();
            }
        });

        ImageView icon = findViewById(R.id.container_full_icon_image);
        final int[] tints = new int[]{ getColor(R.color.brown), getColor(R.color.brown_mask)  };

        ConstraintLayout button = findViewById(R.id.container_full_qr_code);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new QrCodeGeneratorDialog(ContainerActivity.this, getLayoutInflater(), icon.getDrawable(), tints, containerName,
                        ID.toHex(containerId), BarcodeFormat.QR_CODE, true).show();
            }
        });

        TextView name = findViewById(R.id.container_full_name);
        name.setText(containerName);

        TextView location = findViewById(R.id.container_full_location);
        location.setText(containerLocation);

        stateShimmer = findViewById(R.id.container_full_state_shimmer);
        detailsShimmer = findViewById(R.id.container_full_details_shimmer);

        View dummyHeader = findViewById(R.id.dummy_header);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) dummyHeader.getLayoutParams();
        params.setMargins(0, LayoutHelper.getStatusBarHeight(this), 0, 0);

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.container_full_swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemsNotLoaded();
                contentNotLoaded();
                Fetcher.getInstance().fetchContainer(containerId, new APIResponse<Container>() {
                    @Override
                    public void response(Container result) {
                        ContainerActivity.this.container = result;
                        swipeRefreshLayout.setRefreshing(false);
                        showContainer();
                    }

                    @Override
                    public void error(RequestError error) {
                        System.err.println("Failed to fetch container data: " + error);
                        Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_container, error.toString()), Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.green);
        swipeRefreshLayout.setProgressViewOffset(true, swipeRefreshLayout.getProgressViewStartOffset(), swipeRefreshLayout.getProgressViewEndOffset());

        itemsLayout = findViewById(R.id.container_full_items_layout);
        itemShimmer = findViewById(R.id.container_full_item_shimmer);
        final RecyclerView items = findViewById(R.id.container_full_items);
        items.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        items.setNestedScrollingEnabled(false);

        Fetcher.getInstance().fetchContainer(containerId, new APIResponse<Container>() {
            @Override
            public void response(Container result) {
                ContainerActivity.this.container = result;
                showContainer();
            }

            @Override
            public void error(RequestError error) {
                System.err.println("Failed to fetch container data: " + error);
                Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_container, error.toString()), Toast.LENGTH_LONG).show();
                finish();
            }
        });

        findViewById(R.id.container_full_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new IntentBuilder(ContainerActivity.this, CreateObjectActivity.class)
                    .scope("update").type("container").blueprint(container).build(), 0);
            }
        });
    }

    private void showContainer(){
        contentLoaded();

        TextView name = findViewById(R.id.container_full_name);
        name.setText(container.getContent());

        if(container.getLocations().size() > 0){
            final String lastLocation = container.getLocations().get(0).getLocation();
            TextView location = findViewById(R.id.container_full_location);
            location.setText(lastLocation);
        }

        TextView state = findViewById(R.id.container_full_state);
        String stateTxr = getString(R.string.full_page_no_state);
        if (container.getState() != null && !container.getState().isEmpty()) {
            stateTxr = container.getState();
        }
        state.setText(stateTxr);

        TextView details = findViewById(R.id.container_full_details);
        details.setText(container.getDetails());

        RecyclerView locationPoints = findViewById(R.id.container_full_locations);
        locationPoints.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        LocationPointAdapter adapter = new LocationPointAdapter(this, container.getLocations());
        locationPoints.setAdapter(adapter);

        loadContent();
    }

    private void loadContent() {
        final RecyclerView items = findViewById(R.id.container_full_items);
        Fetcher.getInstance().fetchItems(container.getItems(), new APIResponse<List<Item>>() {
            @Override
            public void response(List<Item> result) {
                ItemListAdapter adapter = new ItemListAdapter(ContainerActivity.this, result,
                        data -> LayoutHelper.openItem(ContainerActivity.this, data));
                items.setAdapter(adapter);

                final SearchView itemSearch = findViewById(R.id.container_full_search_items);
                itemSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        adapter.getFilter().filter(s);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        adapter.getFilter().filter(s);
                        return true;
                    }
                });

                itemsLoaded();
            }

            @Override
            public void error(RequestError error) {
                System.err.println("Could not fetch items: " + error.toString());
                runOnUiThread(() -> Toast.makeText(ContainerActivity.this,
                        getString(R.string.full_page_error_items, error.toString()), Toast.LENGTH_LONG).show());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 1) {
            itemsNotLoaded();
            contentNotLoaded();
            Fetcher.getInstance().fetchContainer(container.getId(), new APIResponse<Container>() {
                @Override
                public void response(Container result) {
                    ContainerActivity.this.container = result;
                    showContainer();
                }

                @Override
                public void error(RequestError error) {
                    System.err.println("Failed to fetch container data: " + error);
                    Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_container, error.toString()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void itemsLoaded() {
        itemShimmer.setVisibility(View.GONE);
        itemsLayout.setVisibility(View.VISIBLE);
    }

    private void itemsNotLoaded() {
        itemShimmer.setVisibility(View.VISIBLE);
        itemsLayout.setVisibility(View.GONE);
    }

    private void contentNotLoaded(){
        TextView state = findViewById(R.id.container_full_state);
        TextView details = findViewById(R.id.container_full_details);

        LayoutHelper.hide(state, details);
        LayoutHelper.show(stateShimmer, detailsShimmer);
    }

    private void contentLoaded(){
        TextView state = findViewById(R.id.container_full_state);
        TextView details = findViewById(R.id.container_full_details);

        LayoutHelper.hide(stateShimmer, detailsShimmer);
        LayoutHelper.show(state, details);
    }
}