package com.github.sofiman.inventory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.Tracker;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.impl.APIResponse;
import com.github.sofiman.inventory.model.ItemCompactListAdapter;
import com.github.sofiman.inventory.model.ItemListAdapter;
import com.github.sofiman.inventory.model.TrackerListAdapter;
import com.github.sofiman.inventory.ui.components.ItemCompactComponent;
import com.github.sofiman.inventory.ui.components.ItemComponent;
import com.github.sofiman.inventory.ui.dialogs.QrCodeGeneratorDialog;
import com.github.sofiman.inventory.ui.dialogs.SelectiveAddDialog;
import com.github.sofiman.inventory.utils.IntentBuilder;
import com.github.sofiman.inventory.utils.StringUtils;
import com.github.sofiman.inventory.utils.transform.BitmapBorderTransformation;
import com.github.sofiman.inventory.api.ID;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.github.sofiman.inventory.utils.transform.MaskTransformation;
import com.github.sofiman.inventory.utils.transform.PaletteBitmapTransformation;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.zxing.BarcodeFormat;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;

public class InventoryActivity extends AppCompatActivity {

    private List<Tracker> trackers = Arrays.asList(new Tracker("Track B, Main conveyor belt", "", ""),
            new Tracker("Track A, Main conveyor belt", "", ""),
            new Tracker("Track C, Main conveyor belt", "", ""),
            new Tracker("Discharge bay conveyor belt", "", ""),
            new Tracker("Charge bay conveyor belt", "", ""));

    private ShimmerFrameLayout trackerShimmer;
    private RelativeLayout trackersLayout;

    private ShimmerFrameLayout itemShimmer;
    private RelativeLayout itemsLayout;

    private ShimmerFrameLayout stateShimmer;
    private ShimmerFrameLayout locationShimmer;

    private Inventory inventory;

    private AtomicBoolean valid = new AtomicBoolean(false);
    private int clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        valid.set(true);

        final String inventoryName = getIntent().getStringExtra("INVENTORY_NAME");
        final String inventoryIcon = getIntent().getStringExtra("INVENTORY_ICON");
        final byte[] inventoryId = getIntent().getByteArrayExtra("INVENTORY_ID");

        if (inventoryId == null) {
            supportFinishAfterTransition();
            return;
        }

        ImageView back = findViewById(R.id.inventory_full_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valid.set(false);
                supportFinishAfterTransition();
            }
        });

        setIcon(inventoryId, inventoryName, inventoryIcon);

        TextView name = findViewById(R.id.inventory_full_name);
        name.setText(inventoryName);

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.inventory_full_swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemsNotLoaded();
                trackersNotLoaded();
                contentNotLoaded();
                Fetcher.getInstance().fetchInventory(inventoryId, new APIResponse<Inventory>() {
                    @Override
                    public void response(Inventory result) {
                        InventoryActivity.this.inventory = result;
                        showInventory();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void error(RequestError error) {
                        System.err.println("Failed to fetch inventory data: " + error);
                        Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_inventory, error.toString()), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.green);
        swipeRefreshLayout.setProgressViewOffset(true, swipeRefreshLayout.getProgressViewStartOffset(), swipeRefreshLayout.getProgressViewEndOffset());

        View dummyHeader = findViewById(R.id.dummy_header);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) dummyHeader.getLayoutParams();
        params.setMargins(0, LayoutHelper.getStatusBarHeight(this), 0, 0);

        trackersLayout = findViewById(R.id.inventory_full_trackers_layout);
        trackerShimmer = findViewById(R.id.inventory_full_tracker_shimmer);
        final RecyclerView trackers = findViewById(R.id.inventory_full_trackers);
        trackers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        itemsLayout = findViewById(R.id.inventory_full_items_layout);
        itemShimmer = findViewById(R.id.inventory_full_item_shimmer);
        final RecyclerView items = findViewById(R.id.inventory_full_items);
        items.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        stateShimmer = findViewById(R.id.inventory_full_state_shimmer);
        locationShimmer = findViewById(R.id.inventory_full_location_shimmer);

        findViewById(R.id.inventory_full_assign_tracker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clicked++;
                setNotification("You clicked this " + clicked + " times!", view1 -> hideNotification());
            }
        });

        Fetcher.getInstance().fetchInventory(inventoryId, new APIResponse<Inventory>() {
            @Override
            public void response(Inventory result) {
                InventoryActivity.this.inventory = result;
                showInventory();
            }

            @Override
            public void error(RequestError error) {
                System.err.println("Failed to fetch inventory data: " + error);
                Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_inventory, error.toString()), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setIcon(byte[] inventoryId, String inventoryName, String inventoryIcon) {
        int[] tints = new int[2];
        final Drawable[] popupDrawable = {null};
        TextView iconText = findViewById(R.id.inventory_full_icon_text);
        ImageView iconImage = findViewById(R.id.inventory_full_icon_image);
        ImageView bg = findViewById(R.id.inventory_full_header_bg);
        if (inventoryIcon != null && !URLUtil.isValidUrl(inventoryIcon)) {
            iconImage.setVisibility(View.GONE);
            iconText.setVisibility(View.VISIBLE);
            String[] part = inventoryIcon.split(":");

            iconText.setText(part[0]);
            if (part.length == 2) {
                int color = Color.parseColor(part[1]);
                int bgColor = Color.argb(51, Color.red(color), Color.green(color), Color.blue(color));
                iconText.setTextColor(ColorStateList.valueOf(color));
                iconText.setBackgroundTintList(ColorStateList.valueOf(bgColor));
                tints[0] = color;
                tints[1] = bgColor;
                popupDrawable[0] = ContextCompat.getDrawable(this, R.drawable.icon_awesome_warehouse);
                bg.setBackground(null);
                bg.setImageResource(R.drawable.liquid_footer);
                bg.setImageTintList(ColorStateList.valueOf(color));
                bg.setImageTintMode(PorterDuff.Mode.SRC_OVER);
            }
        } else if (inventoryIcon != null) {
            bg.setBackgroundTintList(null);
            iconText.setText(inventoryName.substring(0, 1).toUpperCase());

            Picasso.get().load(inventoryIcon).fit().error(R.drawable.icon_awesome_warehouse).placeholder(R.color.placeholder_color)
                    .transform(new BitmapBorderTransformation(0, 32, 0)).into(iconImage, new Callback() {
                @Override
                public void onSuccess() {
                    iconText.setVisibility(View.INVISIBLE);
                    iconImage.setVisibility(View.VISIBLE);
                    popupDrawable[0] = iconImage.getDrawable();
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }

        ConstraintLayout button = findViewById(R.id.inventory_full_qr_code);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new QrCodeGeneratorDialog(InventoryActivity.this, getLayoutInflater(), popupDrawable[0], tints, inventoryName,
                        ID.toHex(inventoryId), BarcodeFormat.QR_CODE, false).show();
            }
        });
    }

    private void buildGlass(ImageView back, ImageView bg) {
        final int defaultColor = getColor(R.color.iconAccent),
                glass = getColor(R.color.glass);
        String url = inventory.getBackground();
        if(url == null || url.length() == 0){
            url = inventory.getIcon();
        }
        PaletteBitmapTransformation transform = new PaletteBitmapTransformation();
        Picasso.get().load(url).fit()
                .transform(Arrays.asList(
                        transform,
                        new ColorFilterTransformation(glass),
                        new BlurTransformation(this, 25),
                        new BlurTransformation(this, 25),
                        new BlurTransformation(this, 25),
                        new MaskTransformation(InventoryActivity.this, R.drawable.liquid_header)
                ))
                .into(bg, new Callback() {
                    @Override
                    public void onSuccess() {
                        final Palette palette = transform.getPalette();
                        if (palette != null) {
                            int muted = palette.getMutedColor(defaultColor);
                            back.setImageTintList(ColorStateList.valueOf(muted));
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    private void showInventory() {
        if (!valid.get()) return;
        contentLoaded();
        setIcon(inventory.getId(), inventory.getName(), inventory.getIcon());

        TextView name = findViewById(R.id.inventory_full_name);
        name.setText(inventory.getName());

        TextView location = findViewById(R.id.inventory_full_location);
        location.setText(inventory.getLocation());

        TextView state = findViewById(R.id.inventory_full_state);
        String stateTxr = getString(R.string.full_page_no_state);
        if (inventory.getState() != null && !inventory.getState().isEmpty()) {
            stateTxr = inventory.getState();
        }

        state.setText(stateTxr);

        String inventoryIcon = inventory.getIcon();
        ImageView bg = findViewById(R.id.inventory_full_header_bg);
        if (inventoryIcon != null && URLUtil.isValidUrl(inventoryIcon)) {
            ImageView back = findViewById(R.id.inventory_full_back);
            bg.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.placeholder_color)));
            bg.setImageTintList(null);
            buildGlass(back, bg);
        } else {
            bg.setImageResource(R.drawable.liquid_header);
            bg.setImageTintList(ColorStateList.valueOf(getColor(R.color.placeholder_color)));
            try {
                int color = Color.parseColor(inventoryIcon.split(":")[1]);
                int bgColor = Color.argb(51, Color.red(color), Color.green(color), Color.blue(color));
                bg.setImageTintList(ColorStateList.valueOf(bgColor));
                bg.setImageTintMode(PorterDuff.Mode.SRC_OVER);
                ImageView back = findViewById(R.id.inventory_full_back);
                back.setImageTintList(ColorStateList.valueOf(color));
            } catch (NullPointerException ignored){
            }
        }

        findViewById(R.id.inventory_full_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new IntentBuilder(InventoryActivity.this, CreateObjectActivity.class)
                    .icon(inventoryIcon).background(inventory.getBackground())
                        .scope("update").type("inventory").blueprint(inventory)
                    .build(), 0);
            }
        });

        loadContent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 1) {
            itemsNotLoaded();
            trackersNotLoaded();
            contentNotLoaded();
            Fetcher.getInstance().fetchInventory(inventory.getId(), new APIResponse<Inventory>() {
                @Override
                public void response(Inventory result) {
                    InventoryActivity.this.inventory = result;
                    showInventory();
                }

                @Override
                public void error(RequestError error) {
                    System.err.println("Failed to fetch inventory data: " + error);
                    Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_inventory, error.toString()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadContent() {
        final RecyclerView items = findViewById(R.id.inventory_full_items);
        final SearchView itemSearch = findViewById(R.id.inventory_full_search_items);
        Fetcher.getInstance().fetchItems(inventory.getItems(), new APIResponse<List<Item>>() {
            @Override
            public void response(List<Item> result) {
                ItemListAdapter adapter = new ItemListAdapter(InventoryActivity.this, result, new com.github.sofiman.inventory.utils.Callback<Pair<Item, ItemComponent>>() {
                    @Override
                    public void run(Pair<Item, ItemComponent> data) {
                        LayoutHelper.openItem(InventoryActivity.this, data);
                    }
                });
                items.setAdapter(adapter);

                itemSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        adapter.getFilter().filter(s);
                        return true;
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
                runOnUiThread(() -> Toast.makeText(InventoryActivity.this,
                        getString(R.string.full_page_error_items, error.toString()), Toast.LENGTH_LONG).show());
            }
        });
        final RecyclerView trackers = findViewById(R.id.inventory_full_trackers);
        trackers.setAdapter(new TrackerListAdapter(this, this.trackers));
        trackersLoaded(); // TODO: Loading the trackers
    }

    private void trackersLoaded() {
        trackerShimmer.setVisibility(View.GONE);
        trackersLayout.setVisibility(View.VISIBLE);
    }

    private void trackersNotLoaded() {
        trackerShimmer.setVisibility(View.VISIBLE);
        trackersLayout.setVisibility(View.GONE);
    }

    private void itemsLoaded() {
        itemShimmer.setVisibility(View.GONE);
        itemsLayout.setVisibility(View.VISIBLE);
    }

    private void itemsNotLoaded() {
        itemShimmer.setVisibility(View.VISIBLE);
        itemsLayout.setVisibility(View.GONE);
    }

    private void contentNotLoaded() {
        TextView location = findViewById(R.id.inventory_full_location);
        TextView state = findViewById(R.id.inventory_full_state);

        LayoutHelper.hide(location, state);
        LayoutHelper.show(stateShimmer, locationShimmer);
        stateShimmer.startShimmer();
        locationShimmer.startShimmer();
    }

    private void contentLoaded() {
        TextView location = findViewById(R.id.inventory_full_location);
        TextView state = findViewById(R.id.inventory_full_state);

        stateShimmer.stopShimmer();
        locationShimmer.stopShimmer();
        LayoutHelper.hide(stateShimmer, locationShimmer);
        LayoutHelper.show(location, state);
    }

    private void setNotification(String notificationText, View.OnClickListener onClick) {
        TextView content = findViewById(R.id.inventory_full_notification_content);
        content.setText(notificationText);
        ConstraintLayout notification = findViewById(R.id.inventory_full_notification);
        notification.setVisibility(View.VISIBLE);
        notification.animate().translationX(0).alpha(1f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                notification.setOnClickListener(onClick);
            }
        });
    }

    private void hideNotification() {
        ConstraintLayout notification = findViewById(R.id.inventory_full_notification);

        notification.setOnClickListener(null);
        notification.animate()
                .translationX(notification.getWidth() + 100)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        notification.setVisibility(View.GONE);
                        notification.setAlpha(1.0f);
                    }
                });
    }

}