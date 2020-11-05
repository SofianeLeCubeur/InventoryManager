package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.utils.transform.BitmapBorderTransformation;
import com.github.sofiman.inventory.utils.StringUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * TODO: document your custom view class.
 */
public class InventoryComponent extends Component {

    private String name;
    private String icon;
    private String location;
    private int itemCount, trackerCount;

    public InventoryComponent(Context context) {
        super(context, R.layout.component_inventory);
    }

    public InventoryComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_inventory);
    }

    public InventoryComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_inventory);
    }

    @Override
    protected void setup(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.InventoryComponent, defStyle, 0);

        final TypedArray b = getContext().obtainStyledAttributes(
                attrs, R.styleable.Object, defStyle, 0);

        name = b.getString(R.styleable.Object_name);
        location = b.getString(R.styleable.Object_location);

        icon = a.getString(R.styleable.InventoryComponent_iconUri);
        itemCount = a.getInt(R.styleable.InventoryComponent_itemCount, 0);
        trackerCount = a.getInt(R.styleable.InventoryComponent_trackerCount, 0);

        a.recycle();
        b.recycle();
    }

    public void update() {
        TextView iconText = findViewById(R.id.inventory_icon_text);
        ImageView iconImage = findViewById(R.id.iventory_icon_image);
        if (this.icon != null && !URLUtil.isValidUrl(this.icon)) {
            iconImage.setVisibility(GONE);
            iconText.setVisibility(VISIBLE);
            String[] part = this.icon.split(":");

            iconText.setText(part[0]);
            if (part.length == 2) {
                int color = Color.parseColor(part[1]);
                int bgColor = Color.argb(51, Color.red(color), Color.green(color), Color.blue(color));
                iconText.setTextColor(ColorStateList.valueOf(color));
                iconText.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            }
        } else if (this.icon != null) {
            iconImage.setVisibility(INVISIBLE);
            iconText.setText(name.substring(0, 1).toUpperCase());

            Picasso.get().load(icon).fit().error(R.drawable.icon_awesome_warehouse).placeholder(R.color.placeholder_color)
                    .transform(new BitmapBorderTransformation(0, 32, 0)).into(iconImage, new Callback() {
                @Override
                public void onSuccess() {
                    iconText.setVisibility(INVISIBLE);
                    iconImage.setVisibility(VISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("Failed to load inventory icon for " + getName()  + " : " + e.getMessage());
                }
            });
        }
        TextView name = findViewById(R.id.inventory_name);
        name.setText(this.name);
        TextView itemCount = findViewById(R.id.inventory_item_count);
        itemCount.setText(String.valueOf(this.itemCount));
        TextView trackerCount = findViewById(R.id.inventory_tracker_count);
        trackerCount.setText(String.valueOf(this.trackerCount));
        TextView location = findViewById(R.id.inventory_location);
        location.setText(StringUtils.limit(this.location, 30));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getTrackerCount() {
        return trackerCount;
    }

    public void setTrackerCount(int trackerCount) {
        this.trackerCount = trackerCount;
    }
}
