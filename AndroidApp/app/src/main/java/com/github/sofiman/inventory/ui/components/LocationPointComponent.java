package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.utils.StringUtils;

public class LocationPointComponent extends ContinousComponent {

    private String location;
    private String timestamp;
    private int padding;

    public LocationPointComponent(Context context) {
        super(context, R.layout.component_location_point);
    }

    public LocationPointComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_location_point);
    }

    public LocationPointComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_location_point);
    }

    @Override
    protected void setup(AttributeSet attrs, int defStyle) {
        Resources r = getResources();
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, r.getDisplayMetrics());
        /*topStateBackground = R.drawable.white_box_ripple_top;
        bottomStateBackground = R.drawable.white_box_ripple_bottom;
        middleStateBackground = R.drawable.white_ripple;
        mixedStateBackground = R.drawable.white_box_ripple;*/
    }

    public void update(){
        TextView content = findViewById(R.id.location_point_location);
        content.setText(this.location);

        TextView timestamp = findViewById(R.id.location_point_timestamp);
        timestamp.setText(this.timestamp);

        getChildAt(0).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                StringUtils.setClipboard(getContext(), "inventorymanager_location", location);
                Toast.makeText(getContext(), getContext().getString(R.string.content_copied, getContext().getString(R.string.full_page_location_point)), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setFirstInContinuity() {
        LinearLayout topHalf = findViewById(R.id.location_point_continuity_top);
        LinearLayout bottomHalf = findViewById(R.id.location_point_continuity_bottom);

        topHalf.setVisibility(INVISIBLE);
        bottomHalf.setVisibility(VISIBLE);

        View v = getChildAt(0);
        v.setTag("continuity:top");
        v.setBackgroundResource(R.drawable.white_box_ripple_top);
        v.setPadding(padding, padding / 4, padding, 0);
    }

    public void resetContinuity(){
        LinearLayout topHalf = findViewById(R.id.location_point_continuity_top);
        LinearLayout bottomHalf = findViewById(R.id.location_point_continuity_bottom);

        topHalf.setVisibility(VISIBLE);
        bottomHalf.setVisibility(VISIBLE);

        for (int i = 0; i < bottomHalf.getChildCount(); i++) {
            bottomHalf.getChildAt(i).setAlpha(1f);
        }
        View v = getChildAt(0);
        v.setTag("continuity:basic");
        v.setBackgroundResource(R.drawable.white_box_ripple);
        v.setPadding(padding, 0, padding, 0);
    }

    public void setLastInContinuity() {
        LinearLayout topHalf = findViewById(R.id.location_point_continuity_top);
        LinearLayout bottomHalf = findViewById(R.id.location_point_continuity_bottom);

        topHalf.setVisibility(VISIBLE);

        float alpha = 1;
        float step = alpha / bottomHalf.getChildCount();
        for (int i = 0; i < bottomHalf.getChildCount(); i++) {
            bottomHalf.getChildAt(i).setAlpha(alpha);
            alpha -= step;
        }

        View v = getChildAt(0);
        if(v.getTag().toString().equals("continuity:top")) {
            v.setTag("continuity:mixed");
            v.setBackgroundResource(R.drawable.white_box_ripple);
            v.setPadding(padding, padding / 4, padding, padding / 4);
        } else {
            v.setTag("continuity:bottom");
            v.setBackgroundResource(R.drawable.white_box_ripple_bottom);
            v.setPadding(padding, 0, padding, padding / 4);
        }
    }

    public void setAsCreate(){
        ImageView create = findViewById(R.id.location_point_add);
        create.setVisibility(VISIBLE);
    }

    public void resetCreate(){
        ImageView create = findViewById(R.id.location_point_add);
        create.setVisibility(GONE);
    }
}
