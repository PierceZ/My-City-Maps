package com.piercezaifman.mycitymaps.util;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.widget.TextView;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Util class to set custom fonts.
 * <p>
 * Created by piercezaifman on 2016-12-15.
 */

public final class FontHelper {

    //TODO this can actually be done with databinding, see https://plus.google.com/+LisaWrayZeitouni/posts/LTr5tX5M9mb

    @Retention(SOURCE)
    @IntDef({FONT_MEDIUM, FONT_LIGHT})
    @interface Font {
    }

    public static final int FONT_MEDIUM = 0;
    public static final int FONT_LIGHT = 1;

    private FontHelper() {
        // private constructor
    }

    public static void setFont(@NonNull Context context, @Font int font, @NonNull TextView textView) {
        switch (font) {
            case FontHelper.FONT_MEDIUM:
                Typeface mediumTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto_medium.ttf");
                textView.setTypeface(mediumTypeface);
                break;
            case FontHelper.FONT_LIGHT:
                Typeface lightTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto_light.ttf");
                textView.setTypeface(lightTypeface);
                break;
            default:
                break;
        }
    }
}
