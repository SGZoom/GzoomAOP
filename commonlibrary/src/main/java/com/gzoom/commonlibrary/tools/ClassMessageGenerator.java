package com.gzoom.commonlibrary.tools;


import java.lang.reflect.Method;

/**
 * Copyright (c) 2020 Tencent. All rights reserved.
 *
 * @author GZoom
 * @date 2020-07-22 21:13
 */
public class ClassMessageGenerator {

    public static String getDesc(final Method method) {
        final StringBuffer buf = new StringBuffer();
        buf.append("(");
        final Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; ++i) {
            buf.append(getDesc(types[i]));
        }
        buf.append(")");
        buf.append(getDesc(method.getReturnType()));
        return buf.toString();
    }

    public static String getDesc(final Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getPrimitiveLetter(returnType);
        }
        if (returnType.isArray()) {
            return "[" + getDesc(returnType.getComponentType());
        }
        return "L" + getType(returnType) + ";";
    }

    public static String getType(final Class<?> parameterType) {
        if (parameterType.isArray()) {
            return "[" + getDesc(parameterType.getComponentType());
        }
        if (!parameterType.isPrimitive()) {
            final String clsName = parameterType.getName();
            return clsName.replaceAll("\\.", "/");
        }
        return getPrimitiveLetter(parameterType);
    }

    // Todo:获取数组类型的
    public static String getType(final String parameterType) {
        // 数组类型还没想好怎么处理 int[]
//        if (parameterType.isArray()) {
//            return "[" + getDesc(parameterType.getComponentType());
//        }
        String type = getPrimitiveLetter(parameterType);
        if (type == null) {
            return "L" + parameterType.replaceAll("\\.", "/") + ";";
        } else {
            return type;
        }
    }

    public static String getPrimitiveLetter(final Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        }
        if (Void.TYPE.equals(type)) {
            return "V";
        }
        if (Boolean.TYPE.equals(type)) {
            return "Z";
        }
        if (Character.TYPE.equals(type)) {
            return "C";
        }
        if (Byte.TYPE.equals(type)) {
            return "B";
        }
        if (Short.TYPE.equals(type)) {
            return "S";
        }
        if (Float.TYPE.equals(type)) {
            return "F";
        }
        if (Long.TYPE.equals(type)) {
            return "J";
        }
        if (Double.TYPE.equals(type)) {
            return "D";
        }
        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    public static String getPrimitiveLetter(String className) {
        if ("int".equals(className)) {
            return "I";
        }
        if ("void".equals(className)) {
            return "V";
        }
        if ("boolean".equals(className)) {
            return "Z";
        }
        if ("character".equals(className)) {
            return "C";
        }
        if ("byte".equals(className)) {
            return "B";
        }
        if ("short".equals(className)) {
            return "S";
        }
        if ("float".equals(className)) {
            return "F";
        }
        if (Long.class.getSimpleName().equals(className)) {
            return "J";
        }
        if (Double.class.getSimpleName().equals(className)) {
            return "D";
        }
        return null;
    }

    private boolean isArrayType(String param) {
        if (param == null || param.isEmpty() || param.length() < 2) {
            return false;
        }
        String lastTwo = param.substring(param.length() - 2);
        return "[]".equals(lastTwo);
    }

    /**
     * 将类名转换成类路径
     * com.a.b -> com/a/b
     */
    public static String getClassPath(String className) {
        if (className == null || className.length() <= 0) {
            return null;
        }
        String[] paths = className.split("\\.");
        String type = getType(className);
        StringBuilder pathBuilder = new StringBuilder("");
        for (String folder : paths) {
            pathBuilder.append(folder);
            pathBuilder.append("/");
        }
        String classPath = pathBuilder.toString();
        if (classPath != null && classPath.length() > 1) {
            classPath = classPath.substring(0, classPath.length() - 1);
        }
        return type + classPath;
    }
}
