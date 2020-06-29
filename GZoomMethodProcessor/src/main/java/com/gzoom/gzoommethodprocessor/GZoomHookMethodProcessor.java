package com.gzoom.gzoommethodprocessor;

import com.google.auto.service.AutoService;
import com.gzoom.commonlibrary.GZoomDataManager;
import com.gzoom.commonlibrary.GZoomMethod;
import com.gzoom.commonlibrary.GZoomMethodInfo;


import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Copyright (c) 2020 GongZaiChang. All rights reserved.
 *
 * @author GZoom
 * @date 2020-06-26 16:34
 */
@AutoService(Processor.class)
public class GZoomHookMethodProcessor extends AbstractProcessor {
    Filer mFiler;
    Elements mElementUtils;
//    private Map<String, AnnotatedClass> mAnnotatedClassMap;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //filter用来创建新的源文件、class文件以及辅助文件
        mFiler = processingEnvironment.getFiler();
        //elements中包含着操作element的工具方法
        mElementUtils = processingEnvironment.getElementUtils();

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        mAnnotatedClassMap.clear();
        try {
            processGZoomMethod(roundEnvironment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void processGZoomMethod(RoundEnvironment roundEnvironment) {
        // 遍历包含GZoomMethod注解的类
        for (Element element : roundEnvironment.getElementsAnnotatedWith(GZoomMethod.class)) {
            GZoomMethod gZoomMethod = element.getAnnotation(GZoomMethod.class);
            // Todo:这里只保留了hook函数的信息，但是没有处理被hook函数的信息
            // 这里保存映射关系，暂时先采用原来的保存方式
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String className = typeElement.getQualifiedName().toString();

            ExecutableElement executableElement = (ExecutableElement) element;
            // 方法名
            String methodName = executableElement.getSimpleName().toString();
            // 描述符
            String methodDesc = "";
            List<VariableElement> list = (List<VariableElement>) executableElement.getParameters();
            for (VariableElement variableElement : list) {
                methodDesc += variableElement.getSimpleName().toString() + ";";
            }
            methodDesc = "(" + methodDesc + ")";
            // 最后要加一个返回类型
            methodDesc += executableElement.getReturnType().toString();
            GZoomMethodInfo methodInfo = new GZoomMethodInfo(className, methodName, methodDesc);
            GZoomDataManager.getInstance().addMethodInfo(methodInfo);
        }
    }


//    /**获取注解所在文件对应的生成类*/
//    private AnnotatedClass getAnnotatedClass(Element element) {
//        //typeElement表示类或者接口元素
//        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
//        String fullName = typeElement.getQualifiedName().toString();
//        //这里其实就是变相获得了注解的类名
//        AnnotatedClass annotatedClass = mAnnotatedClassMap.get(fullName);
//        // Map<String, AnnotatedClass>
//        if (annotatedClass == null) {
//            annotatedClass = new AnnotatedClass(typeElement, mElementUtils);
//            mAnnotatedClassMap.put(fullName, annotatedClass);
//        }
//        return annotatedClass;
//    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(GZoomMethod.class.getCanonicalName());
        return types;
    }
}
