package com.gzoom.aopplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class GZoomAopPlugin implements Plugin<Project>{
    public static final String PLUGIN_NAME = "GZoomAopPlugin"
    @Override
    void apply(Project project) {
        if (!project.getPlugins().hasPlugin(AppPlugin.class)) {
            throw new UnsupportedOperationException("can't work without plugin:'com.android.application'")
        }
        AppExtension android = project.getExtensions().getByType(AppExtension.class)
//        GZoomAopExtension extension = project.getExtensions().create(PLUGIN_NAME, GZoomAopExtension.class, project)
        String path =  project.getPath()
        System.out.println("GZoomAopPlugin 当前的路径："+path)
        // 注册一个Transform
        android.registerTransform(new GZoomTransformation())
    }
}