package com.github.sofiman.inventory.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.api.Webhook;

public class WebhookComponent extends Component {

    private int status;
    private String name;
    private String event;

    public WebhookComponent(Context context) {
        super(context, R.layout.component_webhook);
    }

    public WebhookComponent(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.component_webhook);
    }

    public WebhookComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, R.layout.component_webhook);
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

    public void update() {
        ImageView statusView = findViewById(R.id.webhook_status);
        switch (status) {
            case Webhook.WebhookDelivery.STATUS_SUCCESS:
                setSuccess(statusView);
                break;
            case Webhook.WebhookDelivery.STATUS_ERROR:
                setError(statusView);
                break;
            case Webhook.WebhookDelivery.STATUS_UNKNOWN:
            default:
                setNeutral(statusView);
                break;
        }
        TextView type = findViewById(R.id.webhook_type);
        type.setText(this.event);
        TextView name = findViewById(R.id.webhook_label);
        name.setText(this.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    private void setSuccess(ImageView statusView){
        int color = getContext().getColor(R.color.green);
        int mask = getContext().getColor(R.color.green_mask);
        statusView.setBackgroundTintList(ColorStateList.valueOf(mask));
        statusView.setImageResource(R.drawable.check);
        statusView.setImageTintList(ColorStateList.valueOf(color));
    }

    private void setError(ImageView statusView){
        int color = getContext().getColor(R.color.colorAccent);
        int mask = getContext().getColor(R.color.colorAccentTransparent);
        statusView.setBackgroundTintList(ColorStateList.valueOf(mask));
        statusView.setImageResource(R.drawable.bell_outline);
        statusView.setImageTintList(ColorStateList.valueOf(color));
    }

    private void setNeutral(ImageView statusView){
        int color = getContext().getColor(R.color.white);
        int mask = getContext().getColor(R.color.dark_gray);
        statusView.setBackgroundTintList(ColorStateList.valueOf(mask));
        statusView.setImageResource(R.drawable.icon_awesome_question);
        statusView.setImageTintList(ColorStateList.valueOf(color));
    }
}
