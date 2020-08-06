package com.gzoom.aopplugin

import com.gzoom.commonlibrary.GZoomMethodInfo
import com.gzoom.commonlibrary.file.FileResourceUtils


class GZoomDataManager {
    static GZoomDataManager mInstance
    static HashMap<GZoomMethodInfo, GZoomMethodInfo> mDatas

    private GZoomDataManager() {}

    static GZoomDataManager getInstance() {
        synchronized (GZoomDataManager.class) {
            if (mInstance == null) {
                mInstance = new GZoomDataManager();
            }
        }
        return mInstance;
    }


    void generateData(String path) {
        if (path == null) {
            return
        }
        mDatas = FileResourceUtils.readMethodInfoFromFile(path)
        println("解析结束")
        if (mDatas != null && mDatas.size() > 0) {
            println("解析到了数据内容" + mDatas.size())
        }
    }
}