package com.gzoom.commonlibrary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyright (c) 2020 GongZaiChang. All rights reserved.
 *
 * GZoomAop hook方法参数配置
 *
 * </p>hookClass hook方法类
 * </p>hookMethodReturn hook方法返回类型（String）
 * </p>hookMethodParams hook方法参数（String）
 *
 * @author GZoom
 * @date 2020-06-26 16:26
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface GZoomMethod {
    Class hookClass();
    String hookMethodReturn();
    String hookMethodParams();
}
