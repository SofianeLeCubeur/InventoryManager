package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.sofiman.inventory.R;

public class ContainerComponent extends Component {

    private String content;
    private String location;

    public ContainerComponent(Context context) {
        super(context,  R.layout.component_container);
    }

    public ContainerComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_container);
    }

    public ContainerComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_container);
    }

    @Override
    protected void setup(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Object, defStyle, 0);

        content = a.getString(R.styleable.Object_name);
        location = a.getString(R.styleable.Object_location);

        a.recycle();
        update();
    }

    public void update(){
        TextView content = findViewById(R.id.container_name);
        content.setText(this.content);
        TextView location = findViewById(R.id.container_location);
        location.setText(this.location);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
