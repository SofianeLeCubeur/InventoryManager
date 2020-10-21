package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.utils.Callback;

/**
 * TODO: document your custom view class.
 */
public class ServerComponent extends ConstraintLayout {

    private String name;
    private String ip;
    private OnClickListener editListener, deleteListener;
    private boolean checked;

    public ServerComponent(Context context) {
        super(context);
        init(null, 0);
    }

    public ServerComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ServerComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.component_server, this);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Object, defStyle, 0);

        name = a.getString(R.styleable.Object_name);
        ip = a.getString(R.styleable.Object_location);

        a.recycle();
        update();
    }

    public void update() {
        TextView name = findViewById(R.id.server_name);
        name.setText(this.name);
        TextView ip = findViewById(R.id.server_ip);
        ip.setText(this.ip);
        if (editListener != null) {
            ImageView edit = findViewById(R.id.server_edit);
            edit.setOnClickListener(editListener);
        }
        if (deleteListener != null) {
            ImageView delete = findViewById(R.id.server_delete);
            delete.setOnClickListener(deleteListener);
        }
        RadioButton radio = findViewById(R.id.server_select);
        radio.setChecked(checked);
    }

    public void setChecked(boolean checked){
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setDeleteListener(OnClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setEditListener(OnClickListener editListener) {
        this.editListener = editListener;
    }

    public void setCheckedListener(Callback<Boolean> callback){
        RadioButton radio = findViewById(R.id.server_select);
        radio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radio.isChecked()){
                    callback.run(true);
                }
            }
        });
    }
}
