package com.github.sofiman.inventory.utils.transform;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

public class GradientTransformation implements Transformation {

    private int startColor;
    private int endColor;

    public GradientTransformation(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int x = source.getWidth();
        int y = source.getHeight();

        Bitmap gradientBitmap = source.copy(source.getConfig(), true);
        Canvas canvas = new Canvas(gradientBitmap);
        LinearGradient grad =
                new LinearGradient(x / 2f, y, x / 2f, y / 2f, startColor, endColor, Shader.TileMode.CLAMP);
        Paint p = new Paint(Paint.DITHER_FLAG);
        p.setShader(null);
        p.setDither(true);
        p.setFilterBitmap(true);
        p.setShader(grad);
        canvas.drawPaint(p);
        source.recycle();
        return gradientBitmap;
    }

    @Override
    public String key() {
        return "Gradient";
    }
}
