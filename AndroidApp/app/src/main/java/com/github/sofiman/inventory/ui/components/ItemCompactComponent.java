package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.utils.transform.BitmapBorderTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ItemCompactComponent extends Component {

    private final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
    private final int color = getContext().getColor(R.color.brown);
    private final int bgColor = getContext().getColor(R.color.brown_mask);
    private final int imgBg = getContext().getColor(R.color.image_background);

    private String icon;
    private String name;
    private boolean active;

    public ItemCompactComponent(Context context) {
        super(context, R.layout.component_item_compact);
    }

    public ItemCompactComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_item_compact);
    }

    public ItemCompactComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_item_compact);
    }

    @Override
    protected void setup(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Object, defStyle, 0);

        final TypedArray b = getContext().obtainStyledAttributes(
                attrs, R.styleable.ItemComponent, defStyle, 0);

        name = a.getString(R.styleable.Object_name);
        icon = b.getString(R.styleable.ItemComponent_itemIcon);

        a.recycle();
        b.recycle();
        update();
    }

    public void update(){
        final ImageView itemIcon = findViewById(R.id.item_icon);

        itemIcon.setImageTintList(ColorStateList.valueOf(color));
        itemIcon.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        itemIcon.setImageResource(R.drawable.cube);
        itemIcon.setPadding(px, px, px, px);

        if(icon != null && URLUtil.isValidUrl(icon)){
            itemIcon.setPadding(0, 0, 0, 0);
            Picasso.get().load(icon).fit().placeholder(R.color.placeholder_color)
                    .transform(new BitmapBorderTransformation(1, 16, 0)).into(itemIcon, new Callback() {
                @Override
                public void onSuccess() {
                    itemIcon.setImageTintList(null);
                    itemIcon.setBackgroundTintList(ColorStateList.valueOf(imgBg));
                }

                @Override
                public void onError(Exception e) {
                    itemIcon.setImageResource(R.drawable.cube);
                    itemIcon.setPadding(px, px, px, px);
                }
            });
        }

        TextView name = findViewById(R.id.item_name);
        name.setText(this.name);

        findViewById(R.id.item_more).setVisibility(active ? VISIBLE : INVISIBLE);
        findViewById(R.id.item_select).setVisibility(active ? INVISIBLE : VISIBLE);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
