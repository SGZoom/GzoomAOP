package com.gzoom.commonlibrary;


import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2020 GongZaiChang. All rights reserved.
 * Todo:这个写法不好，不应该在commonLibrary中加入单例数据保存类
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
