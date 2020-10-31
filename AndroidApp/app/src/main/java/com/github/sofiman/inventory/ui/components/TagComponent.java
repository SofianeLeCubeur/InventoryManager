package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.sofiman.inventory.R;

/**
 * TODO: document your custom view class.
 */
public class TagComponent extends FrameLayout {

    private String name;
    private String id;
    private boolean active;
    private ToggleListener listener;

    public TagComponent(Context context) {
        super(context);
        init(null, 0);
    }

    public TagComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TagComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.component_tag, this);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TagComponent, defStyle, 0);

        boolean active = a.getBoolean(R.styleable.TagComponent_active, false);
        setActive(active);

        String name = a.getString(R.styleable.TagComponent_tagName);
        setName(name);

        this.id = a.getString(R.styleable.TagComponent_tagId);

        a.recycle();

        findViewById(R.id.tag_content).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setActive(!TagComponent.this.active);
                if(listener != null){
                    listener.onToggle(TagComponent.this.active);
                }
            }
        });
    }

    public String getTagId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;

        final TextView view = findViewById(R.id.tag_content);
        view.setText(name);
    }

    public String getName() {
        return name;
    }

    public void setOnToggleListener(ToggleListener listener){
        this.listener = listener;
    }

    public void setActive(boolean active) {
        this.active = active;

        final TextView view = findViewById(R.id.tag_content);
        if(active){
            int color = this.getContext().getColor(R.color.colorAccent);
            int alphaColor = Color.argb(51, Color.red(color), Color.green(color), Color.blue(color));
            view.setBackgroundTintList(ColorStateList.valueOf(alphaColor));
            view.setTextColor(color);
        } else {
            int color = this.getContext().getColor(R.color.foreground);
            int bgColor = this.getContext().getColor(R.color.background);
            view.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            view.setTextColor(color);
        }
    }

    public boolean isActive() {
        return active;
    }

    public static interface ToggleListener {
        void onToggle(boolean state);
    }
}
