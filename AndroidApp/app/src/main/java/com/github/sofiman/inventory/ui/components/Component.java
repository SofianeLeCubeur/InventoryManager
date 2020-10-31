package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public abstract class Component extends ConstraintLayout {

    public Component(Context context, @LayoutRes int resourceId) {
        super(context);
        init(null, 0, resourceId);
    }

    public Component(Context context, AttributeSet attrs, @LayoutRes int resourceId) {
        super(context, attrs);
        init(attrs, 0, resourceId);
    }

    public Component(Context context, AttributeSet attrs, int defStyle, @LayoutRes int resourceId) {
        super(context, attrs, defStyle);
        init(attrs, defStyle, resourceId);
    }

    private void init(AttributeSet attrs, int defStyle, @LayoutRes int resourceId){
        inflate(getContext(), resourceId, this);
        this.setup(attrs, defStyle);
    }

    protected void setup(AttributeSet attrs, int defStyle){
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        getChildAt(0).setOnClickListener(l);
    }

    public abstract void update();
}
