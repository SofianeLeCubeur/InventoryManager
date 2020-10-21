package com.github.sofiman.inventory.utils.transform;

import android.graphics.Bitmap;

import androidx.palette.graphics.Palette;

import com.squareup.picasso.Transformation;

public class PaletteBitmapTransformation implements Transformation {

    private Palette palette;

    @Override
    public Bitmap transform(Bitmap source) {
        this.palette = Palette.from(source).generate();
        return source;
    }

    @Override
    public String key() {
        return "palette";
    }

    public Palette getPalette() {
        return palette;
    }
}
