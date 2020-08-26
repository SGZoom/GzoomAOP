package com.gzoom.aopplugin

import com.android.tools.r8.code.M
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
        SimplifyAopExtension.clearData()
        mDatas.forEach {
            target,source ->
                MethodInfo targetMethod = new MethodInfo()
                targetMethod.className = target.targetClassName
                targetMethod.methodName = target.targetMethodName
                targetMethod.methodDesc = target.targetMethodDesc

                MethodInfo sourceMethod = new MethodInfo()
                sourceMethod.className = source.targetClassName
                sourceMethod.methodName = source.targetMethodName
                sourceMethod.methodDesc = source.targetMethodDesc

                Entry entry = new Entry()
                entry.replaceMethod = targetMethod
                entry.sourceMethod = sourceMethod
                SimplifyAopExtension.addEntry(entry)
        }
    }

    HashMap<GZoomMethodInfo, GZoomMethodInfo> getData() {
        return mDatas
    }
}