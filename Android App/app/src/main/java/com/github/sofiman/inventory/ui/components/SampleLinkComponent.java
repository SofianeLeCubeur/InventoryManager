package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.sofiman.inventory.R;

public class SampleLinkComponent extends Component {

    private String label;

    public SampleLinkComponent(Context context) {
        super(context, R.layout.component_sample_link);
    }

    public SampleLinkComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_sample_link);
    }

    public SampleLinkComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_sample_link);
    }

    @Override
    protected void setup(AttributeSet attrs, int defStyle) {
        super.setup(attrs, defStyle);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Object, defStyle, 0);

        label = a.getString(R.styleable.Object_name);

        a.recycle();
        update();
    }

    public void update(){
        TextView content = findViewById(R.id.link_name);
        content.setText(this.label);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        findViewById(R.id.link_layout).setOnClickListener(l);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
