package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.sofiman.inventory.R;

/**
 * TODO: document your custom view class.
 */
public class InputFieldComponent extends ContinousComponent {

    private @DrawableRes
    int icon;
    private String hint;
    private String content;

    public InputFieldComponent(Context context) {
        super(context, R.layout.component_field);
    }

    public InputFieldComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_field);
    }

    public InputFieldComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_field);
    }

    @Override
    protected void setup(AttributeSet attrs, int defStyle) {
        super.setup(attrs, defStyle);
        topStateBackground = R.drawable.white_box_ripple_top;
        bottomStateBackground = R.drawable.white_box_ripple_bottom;
        middleStateBackground = R.drawable.white_ripple;
        mixedStateBackground = R.drawable.white_box;

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.InputFieldComponent, defStyle, 0);

        icon = a.getResourceId(R.styleable.InputFieldComponent_icon, R.color.transparent);
        hint = a.getString(R.styleable.InputFieldComponent_hint);
        content = a.getString(R.styleable.InputFieldComponent_text);

        a.recycle();
    }

    @Override
    public void update() {
        ImageView icon = findViewById(R.id.field_icon);
        icon.setImageResource(this.icon);
        EditText content = findViewById(R.id.field_content);
        content.setHint(this.hint);
        content.setText(this.content);
        if (this.icon == 0) {
            icon.setVisibility(GONE);
        } else {
            icon.setVisibility(VISIBLE);
        }
    }

    public void focus() {
        EditText content = findViewById(R.id.field_content);
        content.requestFocus();
    }

    public void setEditListener(TextWatcher textWatcher){
        EditText content = findViewById(R.id.field_content);
        content.addTextChangedListener(textWatcher);
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getHint() {
        return hint;
    }
}
