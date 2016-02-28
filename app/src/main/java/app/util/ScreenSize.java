package app.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * Created by Genka on 29.09.2015.
 */
public class ScreenSize {

    private Activity activity;

    public ScreenSize (Activity _activity) {
        this.activity = _activity;
    }

    public int getScreenHeight (float decrease) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return Math.round(displayMetrics.heightPixels / displayMetrics.density / decrease);
    }
    public int getScreenHeight () {
        return getScreenWidth(1);
    }

    public int getScreenWidth (float decrease) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return Math.round(displayMetrics.widthPixels  / displayMetrics.density / decrease);
    }
    public int getScreenWidth () {
       return getScreenWidth(1);
    }
}
