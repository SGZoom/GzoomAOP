package com.gzoom.aopplugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.objectweb.asm.Opcodes


/**
 * Copyright (c) 2020 GongZaiChang. All rights reserved.
 *
 * @author GZoom* @date 2020-06-20 17:29
 */
class GZoomAopClassVisitor extends ClassVisitor {
    private String mClassName

    GZoomAopClassVisitor(int i) {
        super(i)
    }

    GZoomAopClassVisitor(int i, ClassVisitor classVisitor) {
        super(i, classVisitor)
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mClassName = name
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        println("访问：" + mClassName + ",name=" + name + ",desc" + desc)
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        GZoomHookMethodAdapter adapter = new GZoomHookMethodAdapter(Opcodes.ASM5, methodVisitor, access, name, desc)
        adapter.mCurClassName = mClassName
        return methodVisitor
    }
}
