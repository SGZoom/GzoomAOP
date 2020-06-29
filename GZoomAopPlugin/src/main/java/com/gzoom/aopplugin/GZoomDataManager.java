package com.gzoom.aopplugin;


import com.gzoom.commonlibrary.GZoomMethodInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2020 GongZaiChang. All rights reserved.
 *
 * @author GZoom
 * @date 2020-06-26 23:16
 */
public class GZoomDataManager {
    private static GZoomDataManager mInstance;
    private List<GZoomMethodInfo> mMethodInfos = new ArrayList<>();

    private GZoomDataManager() {
    }

    public static GZoomDataManager getInstance() {
        synchronized (GZoomDataManager.class) {
            if (mInstance == null) {
                synchronized (GZoomDataManager.class) {
                    mInstance = new GZoomDataManager();
                }
            }
        }
        return mInstance;
    }

    public void clearMethodInfos() {
        mMethodInfos.clear();
    }

    public void addMethodInfo(GZoomMethodInfo methodInfo) {
        if (methodInfo != null) {
            mMethodInfos.add(methodInfo);
        }
    }

}
