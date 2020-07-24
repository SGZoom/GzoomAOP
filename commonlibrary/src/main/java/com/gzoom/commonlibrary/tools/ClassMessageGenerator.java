package com.gzoom.commonlibrary.tools;

import com.sun.org.apache.regexp.internal.RE;

import java.lang.reflect.Method;

/**
 * Copyright (c) 2020 Tencent. All rights reserved.
 *
 * @author liamjyxu
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

    public static String getType(final String parameterType) {
        // 数组类型还没想好怎么处理 int[]
//        if (parameterType.isArray()) {
//            return "[" + getDesc(parameterType.getComponentType());
//        }
        String type = getPrimitiveLetter(parameterType);
        if (type == null) {
            return parameterType.replaceAll("\\.", "/");
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
        if (Integer.class.getSimpleName().equals(className)) {
            return "I";
        }
        if (Void.class.getSimpleName().equals(className)) {
            return "V";
        }
        if (Boolean.class.getSimpleName().equals(className)) {
            return "Z";
        }
        if (Character.class.getSimpleName().equals(className)) {
            return "C";
        }
        if (Byte.class.getSimpleName().equals(className)) {
            return "B";
        }
        if (Short.class.getSimpleName().equals(className)) {
            return "S";
        }
        if (Float.class.getSimpleName().equals(className)) {
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

}
