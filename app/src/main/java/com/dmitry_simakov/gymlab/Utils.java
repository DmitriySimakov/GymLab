package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static void setImageFromAssets(Context context, ImageView imageView, String imagePath) {
        try {
            InputStream ims = context.getAssets()
                    .open("images/"+ imagePath);
            Drawable drawable = Drawable.createFromStream(ims, null);
            imageView.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
