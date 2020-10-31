package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.utils.Animations;

public class ScanResultComponent extends Component {

    private String content;
    private String timestamp;
    private View body;
    private @DrawableRes int type;
    private boolean expanded;

    public ScanResultComponent(Context context) {
        super(context, R.layout.component_scan_result);
    }

    public ScanResultComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_scan_result);
    }

    public ScanResultComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_scan_result);
    }

    protected void setup(AttributeSet attrs, int defStyle) {
        super.setup(attrs, defStyle);

        ConstraintLayout toggle = findViewById(R.id.scan_result_layout);
        toggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setExpanded(!expanded);
            }
        });

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Object, defStyle, 0);

        content = a.getString(R.styleable.Object_name);

        a.recycle();
        update();
    }

    public void update(){
        TextView content = findViewById(R.id.scan_result_content);
        content.setText(this.content);

        TextView timestamp = findViewById(R.id.scan_result_timestamp);
        timestamp.setText(this.timestamp);

        LinearLayout body = findViewById(R.id.scan_result_body);
        ImageView chevron = findViewById(R.id.scan_result_chevron);
        body.setVisibility(GONE);
        if(this.body != null){
            chevron.setVisibility(VISIBLE);
            body.removeAllViews();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            body.addView(this.body, params);
        } else {
            chevron.setVisibility(INVISIBLE);
        }
        expanded = false;

        ImageView type = findViewById(R.id.scan_result_type);
        type.setImageResource(this.type);
    }

    public void setOnCheckedListener(CompoundButton.OnCheckedChangeListener checkedListener){
        CheckBox checkBox = findViewById(R.id.scan_result_state);
        checkBox.setOnCheckedChangeListener(checkedListener);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setBody(View body) {
        this.body = body;
    }

    public View getBody() {
        return body;
    }

    public void setChecked(boolean checked){
        CheckBox checkBox = findViewById(R.id.scan_result_state);
        checkBox.setChecked(checked);
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        ImageView chevron = findViewById(R.id.scan_result_chevron);
        if(this.body != null){
            chevron.setVisibility(VISIBLE);
            LinearLayout body = findViewById(R.id.scan_result_body);
            if(expanded){
                chevron.animate().rotation(90).setInterpolator(new LinearInterpolator()).setDuration(170);
                Animations.expand(body);
            } else {
                chevron.animate().rotation(0).setInterpolator(new LinearInterpolator()).setDuration(170);
                Animations.collapse(body);
            }
        } else {
            chevron.setVisibility(INVISIBLE);
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
