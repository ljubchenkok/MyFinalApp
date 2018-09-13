package ru.com.penza.myfinalapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;

import com.squareup.picasso.Transformation;

/**
 * Created by Константин on 05.03.2018.
 */

public class ColorTransformation implements Transformation {

    private int color = 0;

    public ColorTransformation(int color) {
        this.color = color;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (color == 0) {
            return source;
        }
        BitmapDrawable drawable = new BitmapDrawable(Resources.getSystem(), source);
        Bitmap result = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        drawable.draw(canvas);
        drawable.setColorFilter(null);
        drawable.setCallback(null);

        if (result != source) {
            source.recycle();
        }

        return result;
    }

    @Override
    public String key() {
        return "DrawableColor: " + color;
    }
}
