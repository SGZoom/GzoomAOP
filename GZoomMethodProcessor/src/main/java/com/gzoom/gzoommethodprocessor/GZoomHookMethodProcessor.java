package com.gzoom.gzoommethodprocessor;

import com.google.auto.service.AutoService;
import com.gzoom.commonlibrary.GZoomMethod;
import com.gzoom.commonlibrary.GZoomMethodInfo;
import com.gzoom.commonlibrary.file.FileResourceUtils;
import com.gzoom.commonlibrary.tools.ClassMessageGenerator;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

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
    //    List<GZoomMethodInfo> mGZoomMethodList = new ArrayList<>();
    Map<GZoomMethodInfo, GZoomMethodInfo> mReplaceMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //filter用来创建新的源文件、class文件以及辅助文件
        mFiler = processingEnvironment.getFiler();
        //elements中包含着操作element的工具方法
        mElementUtils = processingEnvironment.getElementUtils();

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 这里其实会调用两次
        System.out.println("start GZoomHookMethodProcessor");
        try {
            if (roundEnvironment.processingOver()) {
                writeDataToFile();
            } else {
                processGZoomMethod(roundEnvironment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void writeDataToFile() {
        Filer filer = processingEnv.getFiler();

        try {
            FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT,
                    "", FileResourceUtils.getMethodPath());
            System.out.println("写到目录：" + fileObject.toUri());
            OutputStream out = fileObject.openOutputStream();
            for (GZoomMethodInfo gZoomMethodInfo : mReplaceMap.keySet()) {
                FileResourceUtils.writeMethod(gZoomMethodInfo, mReplaceMap.get(gZoomMethodInfo), out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void processGZoomMethod(RoundEnvironment roundEnvironment) throws ClassNotFoundException, NoSuchMethodException {
        // 遍历包含GZoomMethod注解的类
        for (Element element : roundEnvironment.getElementsAnnotatedWith(GZoomMethod.class)) {
            GZoomMethod gZoomMethod = element.getAnnotation(GZoomMethod.class);
            // Todo:这里只保留了hook函数的信息，但是没有处理被hook函数的信息
            // 这里保存映射关系，暂时先采用原来的保存方式
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String className = typeElement.getQualifiedName().toString();

            ExecutableElement executableElement = (ExecutableElement) element;
            printExecutableElement(executableElement);
            // 方法名
            String methodName = executableElement.getSimpleName().toString();
            // 描述符
            StringBuilder methodDesc = new StringBuilder("(");

            ClassLoader classLoader = gZoomMethod.getClass().getClassLoader();

            List<VariableElement> list = (List<VariableElement>) executableElement.getParameters();

//            Class methodClass = classLoader.loadClass(className);
//            Method method = methodClass.getMethod(methodName, methodClass.getClass());
//            methodDesc.append(ClassMessageGenerator.getDesc(method));

            // 得到的是这种格式的
            // makeText(android.content.Context,java.lang.CharSequence,int)
            for (VariableElement variableElement : list) {
                TypeMirror typeMirror = variableElement.asType();
                System.out.println("开始拼接参数" + typeMirror.toString());
//                Class param = classLoader.loadClass(typeMirror.toString());
//                methodDesc.append(ClassMessageGenerator.);
                methodDesc.append(";");
            }
            methodDesc.append(")");
            String methodString = methodDesc.toString();
            // 最后要加一个返回类型
//            methodDesc += executableElement.getReturnType().toString();
            GZoomMethodInfo targetMethodInfo = new GZoomMethodInfo(className, methodName, methodString);

            String sourceClassName = gZoomMethod.hookClass();

            GZoomMethodInfo sourceMethodInfo = new GZoomMethodInfo(sourceClassName, methodName, methodString);
            mReplaceMap.put(targetMethodInfo, sourceMethodInfo);
        }
    }

    private void printExecutableElement(ExecutableElement executableElement) {
        if (executableElement == null) {
            return;
        }
        System.out.println("开始处理注解：executableElement.getDefaultValue() = " + executableElement.getDefaultValue()
                + ",executableElement.getParameters().size() = " + executableElement.getParameters().size()
                + ",executableElement.getReturnType() = " + executableElement.getReturnType()
                + ",executableElement.getTypeParameters().size() = " + executableElement.getTypeParameters().size());
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(GZoomMethod.class.getCanonicalName());
        return types;
    }

}
