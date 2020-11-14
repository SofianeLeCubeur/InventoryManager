package com.github.sofiman.inventory;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.LocationPoint;
import com.github.sofiman.inventory.impl.APIResponse;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.model.InputFieldListAdapter;
import com.github.sofiman.inventory.model.ItemListAdapter;
import com.github.sofiman.inventory.model.LocationPointAdapter;
import com.github.sofiman.inventory.ui.components.ItemComponent;
import com.github.sofiman.inventory.ui.components.TagComponent;
import com.github.sofiman.inventory.ui.dialogs.DoubleEditDialog;
import com.github.sofiman.inventory.ui.dialogs.EditLocationDialog;
import com.github.sofiman.inventory.utils.IntentBuilder;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.github.sofiman.inventory.utils.StringUtils;
import com.github.sofiman.inventory.utils.transform.BitmapBorderTransformation;
import com.github.sofiman.inventory.utils.transform.MaskTransformation;
import com.github.sofiman.inventory.utils.transform.PaletteBitmapTransformation;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;

public class CreateObjectActivity extends AppCompatActivity {

    private InputFieldListAdapter adapter;
    private LocationPointAdapter locationAdapter;
    private ArrayList<DataField> fields;
    private String icon, background;
    private ItemTouchHelper helper;

    private Map<String, TagComponent> tagList = new HashMap<>();
    private String itemActiveTag;
    private List<String> itemIds;
    private  ConstraintLayout extraLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_object);

        final Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        if (bundle == null) {
            supportFinishAfterTransition();
            return;
        }

        fields = bundle.getParcelableArrayList("fields");
        final byte[] objectId = intent.getByteArrayExtra("object_id");
        final String scope = intent.getStringExtra("scope");
        final String type = intent.getStringExtra("type");
        icon = intent.getStringExtra("icon");
        background = intent.getStringExtra("background");

        if (scope == null || type == null || fields == null) {
            supportFinishAfterTransition();
            return;
        }

        // Setup icon, background and back button
        final ImageView back = findViewById(R.id.create_object_back);
        back.setOnClickListener(view -> {
            setResult(2);
            supportFinishAfterTransition();
        });
        updateIcon();

        // Location edition
        if (intent.hasExtra("locations")) {
            List<LocationPoint> points = intent.getParcelableArrayListExtra("locations");
            if (points != null) {
                points.add(0, new LocationPoint(getString(R.string.create_object_update_location), -1));
                LinearLayout locations = findViewById(R.id.create_object_locations);
                locations.setVisibility(View.VISIBLE);

                RecyclerView locationPoints = findViewById(R.id.create_object_full_locations);
                locationPoints.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
                locationPoints.setNestedScrollingEnabled(false);
                locationAdapter = new LocationPointAdapter(this, points);
                locationAdapter.setOnClick(data -> {
                    if (data.getTimestamp() == -1) {
                        openLocationDialog();
                    }
                });
                locationPoints.setAdapter(locationAdapter);
            }
        }

        // Content (items) edition
        LinearLayout items = findViewById(R.id.create_object_item_list);
        if (intent.hasExtra("items")) {
            itemIds = intent.getStringArrayListExtra("items");
            if (itemIds != null) {
                items.setVisibility(View.VISIBLE);
                loadContent(itemIds);
                LinearLayout tags = findViewById(R.id.create_object_items_tags);

                String required = scope.equals("update") ? "added" : "explore";
                for (int i = 0; i < tags.getChildCount(); i++) {
                    final TagComponent component = (TagComponent) tags.getChildAt(i);
                    final String tagId = component.getTagId();
                    this.tagList.put(tagId, component);
                    if (tagId.equals(required)) {
                        component.setActive(true);
                        itemActiveTag = component.getTagId();
                    } else {
                        component.setActive(false);
                    }
                    component.setOnToggleListener(new TagComponent.ToggleListener() {
                        @Override
                        public void onToggle(boolean state) {
                            if (state) {
                                for (Map.Entry<String, TagComponent> tag : tagList.entrySet()) {
                                    if (!tag.getKey().equals(tagId)) {
                                        tag.getValue().setActive(false);
                                    }
                                }
                                itemActiveTag = tagId;
                                updateItemViewState();
                            }
                        }
                    });
                }
                updateItemViewState();
            }
        }

        // Icon and Background edition
        ImageView editIcon = findViewById(R.id.create_object_edit_icon);
        final TextView save = findViewById(R.id.create_object_save);
        if (type.equals("container")) {
             ImageView iconImage = findViewById(R.id.create_object_icon_image);
             TextView iconText = findViewById(R.id.create_object_icon_text);
             ImageView bg = findViewById(R.id.create_object_header_bg);

            iconText.setVisibility(View.INVISIBLE);
            iconImage.setVisibility(View.VISIBLE);
            iconImage.setBackgroundResource(R.drawable.small_icon);
            iconImage.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.brown_mask)));
            iconImage.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
            iconImage.setImageResource(R.drawable.cube);
            iconImage.setImageTintList(ColorStateList.valueOf(getColor(R.color.brown)));
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            iconImage.setPadding(px, px, px, px);

            int darkAccent = getColor(R.color.colorPrimaryDark);
            int bgColor = Color.argb(82, Color.red(darkAccent), Color.green(darkAccent), Color.blue(darkAccent));
            save.setTextColor(darkAccent);
            save.setBackgroundTintList(ColorStateList.valueOf(bgColor));

            bg.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorAccent)));
            back.setImageTintList(ColorStateList.valueOf(darkAccent));

            editIcon.setVisibility(View.INVISIBLE);
        } else {
            editIcon.setOnClickListener(v -> showEditIconDialog());
        }

        if(intent.hasExtra("extra")){
            final List<String> extraData = intent.getStringArrayListExtra("extra");
            int extraIdx = intent.getIntExtra("extraIdx", 0);
            extraLayout = findViewById(R.id.create_object_extra);
            TextView selection = findViewById(R.id.create_object_selection);
            selection.setVisibility(View.VISIBLE);
            selection.setText(getString(R.string.create_object_selection_state, extraIdx + 1, extraData.size()));
            findViewById(R.id.create_object_scanned_data).setVisibility(View.VISIBLE);
            showScannedData(extraData, extraIdx);
        }

        View dummyHeader = findViewById(R.id.dummy_header);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) dummyHeader.getLayoutParams();
        params.setMargins(0, LayoutHelper.getStatusBarHeight(this), 0, 0);

        RecyclerView recyclerView = findViewById(R.id.create_object_fields);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter = new InputFieldListAdapter(this, fields);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.create_object_save).setOnClickListener(v -> {
            System.out.println("Mutating " + fields.size() + " fields to " + scope + " the " + type);
            if (scope.equals("update")) {
                List<LocationPoint> locationPoints = locationAdapter.getLocations();
                locationPoints.remove(0);
                if (type.equals("inventory")) {
                    Fetcher.getInstance().pushInventory(objectId, adapter.getFields(), itemIds, icon, background, new APIResponse<Inventory>() {
                        @Override
                        public void response(Inventory callback) {
                            apply(new IntentBuilder().inventory(callback).build());
                        }

                        @Override
                        public void error(RequestError error) {
                            printError(error, R.string.create_object_push_inventory);
                        }
                    });
                } else if (type.equals("container")) {
                    Fetcher.getInstance().pushContainer(objectId, adapter.getFields(), itemIds, locationPoints, icon, background, new APIResponse<Container>() {
                        @Override
                        public void response(Container callback) {
                            apply(new IntentBuilder().container(callback).build());
                        }

                        @Override
                        public void error(RequestError error) {
                            printError(error, R.string.create_object_push_container);
                        }
                    });
                } else if (type.equals("item")) {
                    Fetcher.getInstance().pushItem(objectId, adapter.getFields(), locationPoints, icon, background, new APIResponse<Item>() {
                        @Override
                        public void response(Item callback) {
                            apply(new IntentBuilder().item(callback).build());
                        }

                        @Override
                        public void error(RequestError error) {
                            printError(error, R.string.create_object_push_item);
                        }
                    });
                }
            } else if (scope.equals("create")) {
                if (type.equals("inventory")) {
                    Fetcher.getInstance().createInventory(adapter.getFields(), itemIds, icon, background, new APIResponse<Inventory>() {
                        @Override
                        public void response(Inventory callback) {
                            apply(new IntentBuilder().inventory(callback).build());
                        }

                        @Override
                        public void error(RequestError error) {
                            printError(error, R.string.create_object_push_inventory);
                        }
                    });
                } else if (type.equals("container")) {
                    Fetcher.getInstance().createContainer(adapter.getFields(), itemIds, new APIResponse<Container>() {
                        @Override
                        public void response(Container callback) {
                            apply(new IntentBuilder().container(callback).build());
                        }

                        @Override
                        public void error(RequestError error) {
                            printError(error, R.string.create_object_push_container);
                        }
                    });
                } else if (type.equals("item")) {
                    Fetcher.getInstance().createItem(adapter.getFields(), icon, background, new APIResponse<Item>() {
                        @Override
                        public void response(Item callback) {
                            apply(new IntentBuilder().item(callback).build());
                        }

                        @Override
                        public void error(RequestError error) {
                            printError(error, R.string.create_object_push_item);
                        }
                    });
                }
            }
        });
    }

    private void showEditIconDialog() {
        final ImageView back = findViewById(R.id.create_object_back);
        final ImageView bg = findViewById(R.id.create_object_header_bg);
        DoubleEditDialog dialog = new DoubleEditDialog(CreateObjectActivity.this, getLayoutInflater(),
                new DataField(0, icon, getString(R.string.create_object_icon_url)),
                new DataField(0, background, getString(R.string.create_object_background_url)));
        dialog.setButtons(getString(R.string.dialog_save), (dg, which) -> {
            String raw = dialog.getFirstField().getText().toString();
            if (raw.equals("") || URLUtil.isValidUrl(raw) || raw.contains(":")) {
                icon = raw;
                updateIcon();
            }
            raw = dialog.getSecondField().getText().toString();
            if (raw.equals("") || URLUtil.isValidUrl(raw) || raw.contains(":")) {
                background = raw;
                buildGlass(back, bg);
            }
            dg.dismiss();
        }, getString(R.string.dialog_cancel), DoubleEditDialog.DISPOSE);
        dialog.show();
    }

    private void updateItemViewState(){
        if (itemActiveTag.equals("explore")) {
            loadExploreItems();
        } else if (itemActiveTag.equals("added")) {
            loadContent(itemIds);
        }
    }

    private void apply(Intent intent) {
        setResult(1, intent);
        supportFinishAfterTransition();
    }

    private void printError(RequestError error, @StringRes int type) {
        System.err.println("Could not update: " + error.toString());
        runOnUiThread(() -> Toast.makeText(CreateObjectActivity.this,
                getString(R.string.create_object_push_error, getString(type),
                        error.toString()), Toast.LENGTH_LONG).show());
    }

    private void buildGlass(ImageView back, ImageView bg) {
        final int defaultColor = getColor(R.color.iconAccent),
                glass = getColor(R.color.glass);
        String url = background;
        if (url == null || url.length() == 0) {
            url = icon;
        }
        final TextView save = findViewById(R.id.create_object_save);
        PaletteBitmapTransformation transform = new PaletteBitmapTransformation();
        Picasso.get().load(url).fit()
                .transform(Arrays.asList(
                        transform,
                        new ColorFilterTransformation(glass),
                        new BlurTransformation(this, 25),
                        new BlurTransformation(this, 25),
                        new BlurTransformation(this, 25),
                        new MaskTransformation(CreateObjectActivity.this, R.drawable.liquid_header)
                ))
                .into(bg, new Callback() {
                    @Override
                    public void onSuccess() {
                        final Palette palette = transform.getPalette();
                        if (palette != null) {
                            int dark = palette.getMutedColor(defaultColor);
                            back.setImageTintList(ColorStateList.valueOf(dark));

                            int color = palette.getVibrantColor(defaultColor);
                            int bgColor = Color.argb(82, Color.red(color), Color.green(color), Color.blue(color));
                            save.setTextColor(color);
                            save.setBackgroundTintList(ColorStateList.valueOf(bgColor));
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101 && locationAdapter != null) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.dialog_edit_location_granted, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openLocationDialog() {
        new EditLocationDialog(CreateObjectActivity.this, getLayoutInflater(),
                data -> {
                    locationAdapter.getLocations().add(1, data);
                    locationAdapter.notifyDataSetChanged();
                }).show();
    }

    private void updateIcon() {
        final ImageView iconImage = findViewById(R.id.create_object_icon_image);
        final TextView iconText = findViewById(R.id.create_object_icon_text);
        final ImageView bg = findViewById(R.id.create_object_header_bg);
        ImageView back = findViewById(R.id.create_object_back);

        if (icon != null && icon.length() >= 1) {
            if (!URLUtil.isValidUrl(icon)) {
                iconImage.setVisibility(View.GONE);
                iconText.setVisibility(View.VISIBLE);
                String[] part = icon.split(":");

                iconText.setText(part[0]);
                if (part.length == 2) {
                    bg.setImageResource(R.drawable.liquid_header);
                    bg.setImageTintList(ColorStateList.valueOf(getColor(R.color.placeholder_color)));
                    try {
                        int color = Color.parseColor(part[1]);
                        int bgColor = Color.argb(51, Color.red(color), Color.green(color), Color.blue(color));
                        iconText.setTextColor(ColorStateList.valueOf(color));
                        iconText.setBackgroundTintList(ColorStateList.valueOf(bgColor));
                        bg.setImageTintList(ColorStateList.valueOf(bgColor));
                        bg.setImageTintMode(PorterDuff.Mode.SRC_OVER);
                        back.setImageTintList(ColorStateList.valueOf(color));
                        final TextView save = findViewById(R.id.create_object_save);
                        save.setTextColor(color);
                        save.setBackgroundTintList(ColorStateList.valueOf(bgColor));
                    } catch (NullPointerException ignored) {
                    }
                }
            } else {
                iconText.setVisibility(View.INVISIBLE);
                iconImage.setVisibility(View.VISIBLE);
                bg.setImageTintList(null);
                buildGlass(back, bg);

                Picasso.get().load(icon).fit().placeholder(R.color.placeholder_color)
                        .transform(new BitmapBorderTransformation(0, 32, 0)).into(iconImage);
            }
        } else {
            for (DataField field : fields) {
                if (field.getHint().toLowerCase().contains("name") && field.getValue().length() >= 1) {
                    iconText.setVisibility(View.VISIBLE);
                    iconImage.setVisibility(View.INVISIBLE);

                    int color = getColor(R.color.purple), bgColor = getColor(R.color.purple_mask);
                    iconText.setTextColor(ColorStateList.valueOf(color));
                    iconText.setBackgroundTintList(ColorStateList.valueOf(bgColor));
                    iconText.setText(field.getValue().toUpperCase().substring(0, 1));
                    back.setImageTintList(ColorStateList.valueOf(color));

                    break;
                }
            }
        }
    }

    private void setupItemRecycler(List<Item> result, int color, int background, @DrawableRes int actionDrawable, SwipeCallback callback) {
        final RecyclerView items = findViewById(R.id.create_object_items);
        items.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        Drawable deleteDrawable = ContextCompat.getDrawable(this, actionDrawable);
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.small_icon);
        deleteDrawable.setTint(color);
        icon.setTint(background);

        if (helper != null) {
            helper.attachToRecyclerView(null);
        }
        int intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        int intrinsicHeight = deleteDrawable.getIntrinsicHeight();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                ItemListAdapter.ItemHolder holder = (ItemListAdapter.ItemHolder) viewHolder;
                ItemListAdapter adapter = (ItemListAdapter) items.getAdapter();
                if (adapter != null) {
                    final int idx = holder.getAdapterPosition();
                    if (idx < adapter.getItemCount() && adapter.getItems().size() > 0) {
                        callback.onSwipe(holder, adapter, idx);
                    }
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();

                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
                int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;

                deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteDrawable.draw(c);

                int iconTop = itemView.getTop() + 40;
                int iconBottom = itemView.getBottom() - 40;
                int iconLeft = deleteIconLeft - intrinsicWidth;
                int iconRight = deleteIconRight + intrinsicWidth;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                icon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        helper = new ItemTouchHelper(itemTouchHelperCallback);
        helper.attachToRecyclerView(items);
        final SearchView itemSearch = findViewById(R.id.create_object_item_search);
        ItemListAdapter adapter = new ItemListAdapter(CreateObjectActivity.this, result, data -> LayoutHelper.openItem(CreateObjectActivity.this, data));
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

    private void loadContent(List<String> itemIds) {
        final int colorAccent = getColor(R.color.colorAccent);
        final int colorAccentMask = getColor(R.color.colorAccentTransparent);
        Fetcher.getInstance().fetchItems(itemIds, new APIResponse<List<Item>>() {
            @Override
            public void response(List<Item> result) {
                setupItemRecycler(result, colorAccent, colorAccentMask, R.drawable.icon_material_remove, (holder, adapter, idx) -> {
                    Item item = adapter.getItems().remove(idx);
                    adapter.update();
                    CreateObjectActivity.this.itemIds.remove(item.getRawId());
                    adapter.notifyItemRemoved(idx);
                });
            }

            @Override
            public void error(RequestError error) {
                System.err.println("Could not fetch items: " + error.toString());
                runOnUiThread(() -> Toast.makeText(CreateObjectActivity.this,
                        getString(R.string.full_page_error_items, error.toString()), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void loadExploreItems() {
        Gson gson = new Gson();
        HashMap<String, String> m = new HashMap<>();
        m.put("type", "items");
        m.put("name", ".*");
        Fetcher.getInstance().doQuery(m, new APIResponse<HashMap<String, Object>>() {
            @Override
            public void response(HashMap<String, Object> callback) {
                ArrayList<LinkedTreeMap<?, ?>> rawItems = (ArrayList<LinkedTreeMap<?, ?>>) callback.get("items");
                if (rawItems != null) {
                    List<Item> items = new ArrayList<>();

                    JsonObject content;
                    for (LinkedTreeMap<?, ?> json : rawItems) {
                        content = gson.toJsonTree(json).getAsJsonObject();
                        items.add(gson.fromJson(content, Item.class));
                    }

                    final int green = getColor(R.color.green);
                    final int greenMask = getColor(R.color.green_mask);
                    setupItemRecycler(items, green, greenMask, R.drawable.icon_material_add, (holder, adapter, position) -> {
                        List<Item> itemList = adapter.getItems();
                        Item item = itemList.get(position);
                        if (!CreateObjectActivity.this.itemIds.contains(item.getRawId())) {
                            CreateObjectActivity.this.itemIds.add(item.getRawId());
                            Toast.makeText(CreateObjectActivity.this, R.string.create_object_item_added, Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyItemChanged(position);
                    });
                }
            }

            @Override
            public void error(RequestError error) {
                System.err.println(error.toString());
                Toast.makeText(CreateObjectActivity.this, getString(R.string.full_page_error_items, error.toString()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void itemsLoaded() {
        findViewById(R.id.create_object_item_shimmer).setVisibility(View.GONE);
        findViewById(R.id.create_items_layout).setVisibility(View.VISIBLE);
    }

    private interface SwipeCallback {
        void onSwipe(ItemListAdapter.ItemHolder holder, ItemListAdapter adapter, int position);
    }

    private void showScannedData(List<String> extraData, int extraIdx){
        final String extra = extraData.get(extraIdx);

        final LinearLayout backSelection = findViewById(R.id.create_object_extra_back);
        final LinearLayout nextSelection = findViewById(R.id.create_object_extra_next);
        final TextView content = findViewById(R.id.create_object_extra_content);
        final TextView label = findViewById(R.id.create_object_extra_label);
        extraLayout.setVisibility(View.VISIBLE);
        content.setText(extra);
        label.setText(getString(R.string.create_object_extra_data, extraIdx+1, extraData.size()));
        extraLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                StringUtils.setClipboard(CreateObjectActivity.this, "inventorymanager_scanned_data", extra);
                Toast.makeText(CreateObjectActivity.this, getString(R.string.scan_history_selection_copied, getString(R.string.create_object_extra_data)), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        extraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View currentFocus = getCurrentFocus();
                if(currentFocus instanceof EditText){
                    ((EditText) currentFocus).setText(extra);
                }
            }
        });
        if(extraIdx == 0){
            backSelection.setVisibility(View.INVISIBLE);
        } else {
            backSelection.setVisibility(View.VISIBLE);
            backSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showScannedData(extraData, extraIdx - 1);
                }
            });
        }
        if(extraIdx == extraData.size() - 1){
            nextSelection.setVisibility(View.INVISIBLE);
        } else {
            nextSelection.setVisibility(View.VISIBLE);
            nextSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showScannedData(extraData, extraIdx + 1);
                }
            });
        }

    }
}