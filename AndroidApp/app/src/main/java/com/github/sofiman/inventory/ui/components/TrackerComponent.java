package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.sofiman.inventory.R;

public class TrackerComponent extends Component {

    private String icon;
    private String name;
    private String location;

    public TrackerComponent(Context context) {
        super(context, R.layout.component_tracker);
    }

    public TrackerComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_tracker);
    }

    public TrackerComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_tracker);
    }

    @Override
    protected void setup(AttributeSet attrs, int defStyle) {
        super.setup(attrs, defStyle);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Object, defStyle, 0);

        name = a.getString(R.styleable.Object_name);
        location = a.getString(R.styleable.Object_location);

        a.recycle();
        update();
    }

    public void update(){
        ImageView trackerIcon = findViewById(R.id.tracker_icon);
        int color = Color.parseColor("#35CED9");
        int bgColor = Color.argb(51, Color.red(color), Color.green(color), Color.blue(color));
        trackerIcon.setImageTintList(ColorStateList.valueOf(color));
        trackerIcon.setBackgroundTintList(ColorStateList.valueOf(bgColor));

        TextView name = findViewById(R.id.tracker_label);
        name.setText(this.name);
    }

    public void setRightDrawable(@DrawableRes int drawable) {
        ImageView view = findViewById(R.id.tracker_edit);
        view.setImageResource(drawable);
    }

    public void setRightDrawableOnClickListener(OnClickListener clickListener){
        ImageView view = findViewById(R.id.tracker_edit);
        view.setOnClickListener(clickListener);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }
}
