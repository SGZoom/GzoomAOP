package com.gzoom.gzoomaop.hookers;

import com.gzoom.commonlibrary.GZoomMethod;

/**
 * Copyright (c) 2020 Tencent. All rights reserved.
 *
 * @author GZoom
 * @date 2020-07-23 21:46
 */
public class FakeHooker {

    @GZoomMethod(hookClass = "com/gzoom/gzoomaop/Caculater")
    public static int getResult(int a, int b) {
        int result = 0;
        result = a * b;
        return result;
    }
}
