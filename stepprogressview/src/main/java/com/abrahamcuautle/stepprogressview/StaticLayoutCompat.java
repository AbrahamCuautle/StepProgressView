package com.abrahamcuautle.stepprogressview;

import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class StaticLayoutCompat {

    public static StaticLayout obtain(CharSequence text, TextPaint textPaint,
                                      Layout.Alignment aligment, int width, float spacingMult,
                                      float spacingAdd, boolean includePad) {
        StaticLayout sl = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            sl = StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, width)
                    .setAlignment(aligment)
                    .setLineSpacing(spacingAdd, spacingMult)
                    .setIncludePad(includePad)
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .setEllipsizedWidth(width)
                    .setMaxLines(getMaxLines(text))
                    .build();
        } else {
            try {
                Constructor<StaticLayout> constructor = StaticLayout.class.getConstructor(
                        CharSequence.class, int.class, int.class, TextPaint.class, int.class,
                        Layout.Alignment.class, TextDirectionHeuristic.class, float.class, float.class,
                        boolean.class, TextUtils.TruncateAt.class, int.class, int.class
                );
                constructor.setAccessible(true);
                sl = constructor.newInstance(text, 0, text.length(), textPaint, width,
                        aligment, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingMult, spacingAdd,
                        includePad, TextUtils.TruncateAt.END, width, getMaxLines(text));

            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException exception) {
                sl = new StaticLayout(text, 0, text.length(), textPaint, width, aligment,
                        spacingMult, spacingAdd, includePad, TextUtils.TruncateAt.END, width);
            }
        }
        return sl;
    }

    private static int getMaxLines(CharSequence text) {
        int maxLines = 1;
        for (int i=0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c) || c == Character.LINE_SEPARATOR) {
                maxLines++;
            }
        }
        return maxLines;
    }

}
