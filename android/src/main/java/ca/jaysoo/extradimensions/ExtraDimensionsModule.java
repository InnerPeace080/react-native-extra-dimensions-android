package ca.jaysoo.extradimensions;

import java.lang.Math;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

public class ExtraDimensionsModule extends ReactContextBaseJavaModule {

    public ExtraDimensionsModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ExtraDimensions";
    }


    @ReactMethod
    public void getDimentions( Promise promise_){
        try {
            if (getCurrentActivity()==null){
                promise_.reject("ERROR","activity null");
                return;
            }
            final Context ctx = getReactApplicationContext();
            final DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();

            // Get the real display metrics if we are using API level 17 or higher.
            // The real metrics include system decor elements (e.g. soft menu bar).
            //
            // See: http://developer.android.com/reference/android/view/Display.html#getRealMetrics(android.util.DisplayMetrics)
            if (Build.VERSION.SDK_INT >= 17) {
                Display display = getCurrentActivity().getWindowManager().getDefaultDisplay();
                try {
                    Display.class.getMethod("getRealMetrics", DisplayMetrics.class).invoke(display, metrics);
                } catch (InvocationTargetException e) {
                } catch (IllegalAccessException e) {
                } catch (NoSuchMethodException e) {
                }
            }

            WritableMap map = Arguments.createMap();

            map.putDouble("realWindowHeight", getRealHeight(metrics));
            map.putDouble("realWindowWidth", getRealWidth(metrics));
            map.putDouble("statusBarHeight", getStatusBarHeight(metrics));
            map.putDouble("softMenuBarHeight", getSoftMenuBarHeight(metrics));

            promise_.resolve(map);
        }
        catch(Exception e){
            promise_.reject(e);
        }
    }

    private float getStatusBarHeight(DisplayMetrics metrics) {
        final Context ctx = getReactApplicationContext();
        final int heightResId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return
          heightResId > 0
            ? ctx.getResources().getDimensionPixelSize(heightResId) / metrics.density
            : 0;
    }

    private float getSoftMenuBarHeight(DisplayMetrics metrics) {
        if (getCurrentActivity() == null){
            return 0;
        }
        final float realHeight = getRealHeight(metrics);
        final Context ctx = getReactApplicationContext();
        final DisplayMetrics usableMetrics = ctx.getResources().getDisplayMetrics();

        getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int usableHeight = usableMetrics.heightPixels;

        return Math.max(0, realHeight - usableHeight / metrics.density);
    }

    private float getRealHeight(DisplayMetrics metrics) {
        return metrics.heightPixels / metrics.density;
    }

    private float getRealWidth(DisplayMetrics metrics) {
        return metrics.widthPixels / metrics.density;
    }
}
