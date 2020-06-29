package com.gzoom.gzoomaop.hookers;

import android.content.Context;
import android.widget.Toast;

import com.gzoom.commonlibrary.GZoomMethod;

public class ToastHooker {

    @GZoomMethod(hookClass = Toast.class,hookMethodParams = "String",hookMethodReturn = "Toast")
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, "监控中：" + text, duration);
    }
}
