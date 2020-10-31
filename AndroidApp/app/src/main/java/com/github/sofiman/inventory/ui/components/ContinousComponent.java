package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;

import com.github.sofiman.inventory.R;

public abstract class ContinousComponent extends Component {

    protected int topStateBackground = R.drawable.white_box_top;
    protected int bottomStateBackground = R.drawable.white_box_bottom;
    protected int middleStateBackground = R.color.white;
    protected int mixedStateBackground = R.drawable.white_box;

    public ContinousComponent(Context context, int resourceId) {
        super(context, resourceId);
    }

    public ContinousComponent(Context context, AttributeSet attrs, int resourceId) {
        super(context, attrs, resourceId);
    }

    public ContinousComponent(Context context, AttributeSet attrs, int defStyle, int resourceId) {
        super(context, attrs, defStyle, resourceId);
    }

    public void setFirstInContinuity() {
        View v = getChildAt(0);
        v.setTag("continuity:top");
        v.setBackgroundResource(topStateBackground);
    }

    public void resetContinuity(){
        View v = getChildAt(0);
        v.setTag("continuity:basic");
        v.setBackgroundResource(middleStateBackground);
    }

    public void setLastInContinuity() {
        View v = getChildAt(0);
        if(v.getTag().toString().equals("continuity:top")) {
            v.setTag("continuity:mixed");
            v.setBackgroundResource(mixedStateBackground);
        } else {
            v.setTag("continuity:bottom");
            v.setBackgroundResource(bottomStateBackground);
        }
    }
}
