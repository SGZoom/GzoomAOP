package com.gzoom.commonlibrary;

/**
 * Copyright (c) 2020 GongZaiChang. All rights reserved.
 *
 * @author GZoom
 * @date 2020-06-26 18:18
 */
public class GZoomMethodInfo {
    String mClassName = "";
    String mMethodName = "";
    String mMethodDesc = "";

    // com/example/testmodule/MainClass,name=<init>,desc = ()V 三大要素
    public GZoomMethodInfo(String className, String methodName, String methodDesc) {
        mClassName = className;
        mMethodName = methodName;
        mMethodDesc = methodDesc;

        System.out.println("开始处理:mClassName=" + mClassName + ",mMethodName=" + mMethodName + ",mMethodDesc=" + mMethodDesc);
    }
}
