package com.abrahamcuautle.stepprogressview;

import android.content.Context;
import android.util.DisplayMetrics;

public class DxPxUtils {

    public static float dpToPx(Context context, float dp) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float pxToDp(Context context, float px) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
