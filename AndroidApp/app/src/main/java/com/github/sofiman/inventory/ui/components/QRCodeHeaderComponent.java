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

public class QRCodeHeaderComponent extends Component {

    private String icon;
    private String name;

    public QRCodeHeaderComponent(Context context) {
        super(context, R.layout.component_qr_code_header);
    }

    public QRCodeHeaderComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_qr_code_header);
    }

    public QRCodeHeaderComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_qr_code_header);
    }

    @Override
    protected void setup(AttributeSet attrs, int defStyle) {
        super.setup(attrs, defStyle);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Object, defStyle, 0);

        name = a.getString(R.styleable.Object_name);

        a.recycle();
        update();
    }

    public void update(){
        TextView name = findViewById(R.id.qrcode_header_label);
        name.setText(this.name);
    }

    public void setRightDrawableOnClickListener(OnClickListener clickListener){
        ImageView view = findViewById(R.id.qrcode_header_edit);
        view.setOnClickListener(clickListener);
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
}