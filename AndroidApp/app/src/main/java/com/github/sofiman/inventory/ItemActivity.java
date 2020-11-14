package com.github.sofiman.inventory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.impl.APIResponse;
import com.github.sofiman.inventory.impl.Fetcher;
import com.github.sofiman.inventory.impl.RequestError;
import com.github.sofiman.inventory.model.LocationPointAdapter;
import com.github.sofiman.inventory.ui.dialogs.QrCodeGeneratorDialog;
import com.github.sofiman.inventory.utils.IntentBuilder;
import com.github.sofiman.inventory.utils.transform.BitmapBorderTransformation;
import com.github.sofiman.inventory.api.ID;
import com.github.sofiman.inventory.utils.LayoutHelper;
import com.github.sofiman.inventory.utils.transform.GradientTransformation;
import com.github.sofiman.inventory.utils.transform.MaskTransformation;
import com.github.sofiman.inventory.utils.transform.PaletteBitmapTransformation;
import com.google.zxing.BarcodeFormat;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;

public class ItemActivity extends AppCompatActivity {

    private Item item;
    private Collection<ShimmerFrameLayout> shimmers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        ImageView back = findViewById(R.id.item_full_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });

        final String itemName = getIntent().getStringExtra("ITEM_NAME");
        final String itemIcon = getIntent().getStringExtra("ITEM_ICON");
        final byte[] itemId = getIntent().getByteArrayExtra("ITEM_ID");

        if (itemId == null) {
            supportFinishAfterTransition();
            return;
        }

        setIcon(itemId, itemName, itemIcon);

        TextView name = findViewById(R.id.item_full_name);
        name.setText(itemName);

        View dummyHeader = findViewById(R.id.dummy_header);
        shimmers = LayoutHelper.findChildrenByClass(findViewById(R.id.item_full_infos), ShimmerFrameLayout.class);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) dummyHeader.getLayoutParams();
        params.setMargins(0, LayoutHelper.getStatusBarHeight(this), 0, 0);

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.item_full_swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                contentNotLoaded();
                Fetcher.getInstance().fetchItem(itemId, new APIResponse<Item>() {
                    @Override
                    public void response(Item result) {
                        ItemActivity.this.item = result;
                        swipeRefreshLayout.setRefreshing(false);
                        showItem();
                    }

                    @Override
                    public void error(RequestError error) {
                        System.err.println("Failed to fetch item data: " + error);
                        Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_item, error.toString()), Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.green);
        swipeRefreshLayout.setProgressViewOffset(true, swipeRefreshLayout.getProgressViewStartOffset(), swipeRefreshLayout.getProgressViewEndOffset());

        Fetcher.getInstance().fetchItem(itemId, new APIResponse<Item>() {
            @Override
            public void response(Item result) {
                ItemActivity.this.item = result;
                showItem();
            }

            @Override
            public void error(RequestError error) {
                System.err.println("Failed to fetch item data: " + error);
                Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_item, error.toString()), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
                finish();
            }
        });
    }

    private void setIcon(byte[] itemId, String itemName, String itemIcon) {
        int[] tints = new int[2];
        ImageView icon = findViewById(R.id.item_full_icon);

        int color = getColor(R.color.brown);
        int bgColor = getColor(R.color.brown_mask);
        int imageBg = getColor(R.color.image_background);
        final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, getResources().getDisplayMetrics());

        icon.setImageTintList(ColorStateList.valueOf(color));
        icon.setBackgroundTintList(ColorStateList.valueOf(bgColor));

        if (itemIcon != null) {
            icon.setPadding(0, 0, 0, 0);
            Picasso.get().load(itemIcon).fit().placeholder(R.color.placeholder_color)
                    .transform(new BitmapBorderTransformation(0, 32, 0)).into(icon, new Callback() {
                @Override
                public void onSuccess() {
                    icon.setBackgroundTintList(ColorStateList.valueOf(imageBg));
                    icon.setImageTintList(null);
                    tints[0] = 0;
                    tints[1] = getColor(R.color.image_background);
                }

                @Override
                public void onError(Exception e) {
                    icon.setPadding(px, px, px, px);
                    icon.setImageResource(R.drawable.cube);
                }
            });
        } else {
            icon.setPadding(px, px, px, px);
            icon.setImageResource(R.drawable.cube);

            tints[0] = icon.getImageTintList().getDefaultColor();
            tints[1] = icon.getBackgroundTintList().getDefaultColor();
        }

        ConstraintLayout button = findViewById(R.id.item_full_qr_code);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new QrCodeGeneratorDialog(ItemActivity.this, getLayoutInflater(), icon.getDrawable(), tints, itemName,
                        ID.toHex(itemId), BarcodeFormat.QR_CODE, false).show();
            }
        });
    }

    private void buildGlass(ImageView back, ImageView bg) {
        final int defaultColor = getColor(R.color.iconAccent),
                glass = getColor(R.color.glass);
        String url = item.getBackground();
        if (url == null || url.length() == 0) {
            url = item.getIcon();
        }
        PaletteBitmapTransformation transform = new PaletteBitmapTransformation();
        Picasso.get().load(url).fit()
                .transform(Arrays.asList(
                        transform,
                        new ColorFilterTransformation(glass),
                        new BlurTransformation(this, 25),
                        new BlurTransformation(this, 25),
                        new BlurTransformation(this, 25),
                        new MaskTransformation(ItemActivity.this, R.drawable.liquid_header)
                ))
                .into(bg, new Callback() {
                    @Override
                    public void onSuccess() {
                        final Palette palette = transform.getPalette();
                        if (palette != null) {
                            back.setImageTintList(ColorStateList.valueOf(defaultColor));
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    private void showItem() {
        contentLoaded();
        setIcon(item.getId(), item.getName(), item.getIcon());

        TextView internalName = findViewById(R.id.item_full_internal_name);
        internalName.setText(item.getInternalName());

        TextView serialNumber = findViewById(R.id.item_full_serial_number);
        serialNumber.setText(item.getSerialNumber());

        TextView description = findViewById(R.id.item_full_description);
        description.setText(item.getDescription());

        TextView details = findViewById(R.id.item_full_item_details);
        details.setText(item.getDetails());

        TextView state = findViewById(R.id.item_full_state);
        String stateTxr = getString(R.string.full_page_no_state);
        if (item.getState() != null && !item.getState().isEmpty()) {
            stateTxr = item.getState();
        }
        state.setText(stateTxr);

        TextView location = findViewById(R.id.item_full_location);
        if (item.getLocations().size() > 0) {
            location.setText(item.getLocations().get(0).getLocation());
        }

        String itemIcon = item.getIcon();
        ImageView bg = findViewById(R.id.item_full_header_bg);
        if (itemIcon != null && URLUtil.isValidUrl(itemIcon)) {
            ImageView back = findViewById(R.id.item_full_back);
            bg.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.placeholder_color)));
            bg.setImageTintList(null);
            buildGlass(back, bg);
        } else {
            bg.setBackground(null);
            bg.setImageResource(R.drawable.liquid_footer);
            bg.setImageTintList(ColorStateList.valueOf(getColor(R.color.placeholder_color)));
            try {
                int color = Color.parseColor(itemIcon.split(":")[1]);
                int bgColor = Color.argb(51, Color.red(color), Color.green(color), Color.blue(color));
                bg.setImageTintList(ColorStateList.valueOf(bgColor));
                bg.setImageTintMode(PorterDuff.Mode.SRC_OVER);
                ImageView back = findViewById(R.id.item_full_back);
                back.setImageTintList(ColorStateList.valueOf(color));
            } catch (Exception ignored) {
            }
        }

        RecyclerView locationPoints = findViewById(R.id.item_full_locations);
        locationPoints.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        locationPoints.setNestedScrollingEnabled(false);
        LocationPointAdapter adapter = new LocationPointAdapter(this, item.getLocations());
        locationPoints.setAdapter(adapter);

        findViewById(R.id.item_full_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new IntentBuilder(ItemActivity.this, CreateObjectActivity.class)
                        .icon(itemIcon).scope("update").type("item").blueprint(item).build(), 0);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 1) {
            contentNotLoaded();
            Fetcher.getInstance().fetchItem(item.getId(), new APIResponse<Item>() {
                @Override
                public void response(Item result) {
                    ItemActivity.this.item = result;
                    showItem();
                }

                @Override
                public void error(RequestError error) {
                    System.err.println("Failed to fetch item data: " + error);
                    Toast.makeText(getApplicationContext(), getString(R.string.full_page_error_item, error.toString()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void contentNotLoaded() {
        TextView internalName = findViewById(R.id.item_full_internal_name);
        TextView serialNumber = findViewById(R.id.item_full_serial_number);
        TextView description = findViewById(R.id.item_full_description);
        TextView details = findViewById(R.id.item_full_item_details);
        TextView state = findViewById(R.id.item_full_state);
        TextView location = findViewById(R.id.item_full_location);

        LayoutHelper.hide(internalName, serialNumber, description, details, state, location);

        for (ShimmerFrameLayout shimmer : shimmers) {
            shimmer.startShimmer();
            shimmer.setVisibility(View.VISIBLE);
        }
    }

    private void contentLoaded() {
        TextView internalName = findViewById(R.id.item_full_internal_name);
        TextView serialNumber = findViewById(R.id.item_full_serial_number);
        TextView description = findViewById(R.id.item_full_description);
        TextView details = findViewById(R.id.item_full_item_details);
        TextView state = findViewById(R.id.item_full_state);
        TextView location = findViewById(R.id.item_full_location);

        for (ShimmerFrameLayout shimmer : shimmers) {
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
        }

        LayoutHelper.show(internalName, serialNumber, description, details, state, location);
    }
}