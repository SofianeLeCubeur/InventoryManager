package com.github.sofiman.inventory.utils.transform;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

public class BlurTransformation implements Transformation {

    private RenderScript rs;
    private int radius;

    public BlurTransformation(Context context, int radius) {
        rs = RenderScript.create(context);
        this.radius = radius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap blurredBitmap = source.copy(Bitmap.Config.ARGB_8888, true);

        // Allocate memory for Renderscript to work with
        Allocation input = Allocation.createFromBitmap(
                rs,
                blurredBitmap,
                Allocation.MipmapControl.MIPMAP_FULL,
                Allocation.USAGE_SHARED
        );
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);

        // Set the blur radius
        script.setRadius(radius);

        // Start the ScriptIntrinisicBlur
        script.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(blurredBitmap);

        source.recycle();

        return blurredBitmap;
    }

    @Override
    public String key() {
        return "blur";
    }
}