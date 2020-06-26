package com.gzoom.gzoomaop.hookers;

import android.content.Context;
import android.widget.Toast;

public class ToastHooker {
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, "监控中：" + text, duration);
    }
}
