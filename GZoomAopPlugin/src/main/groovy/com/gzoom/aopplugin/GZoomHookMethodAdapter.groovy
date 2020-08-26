package com.gzoom.aopplugin

import com.android.dx.io.OpcodeInfo
import com.gzoom.commonlibrary.GZoomMethodInfo
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class GZoomHookMethodAdapter extends AdviceAdapter {
    String mCurClassName
    private boolean isNeedRemoveDUP
    private boolean isNeedRecordASTORE
    private Stack<OpcodeInfo> mOpcodeStack = new Stack<>()
    /** 构造函数栈 hook构造函数起到结束内的所有构建函数*/
    private Stack<ConstructorMethod> mConstructorMethodStack = new Stack<>()

    protected GZoomHookMethodAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc)
    }

    @Override
    void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.NEW) {
            GZoomMethodInfo sourceMethod
            GZoomMethodInfo targetMethod
            GZoomDataManager.instance.getData()?.forEach {
                key, value ->
                    boolean isMatch = type == key.targetClassName &&
                            mCurClassName != value.targetClassName &&
                            mCurClassName != key.targetClassName &&
                            key.targetMethodName == "<init>"

                    if (isMatch) {
                        sourceMethod = key
                        targetMethod = value
                    }
            }

            if (sourceMethod != null && targetMethod != null) {
                //开始hook构造函数 需要New指令
                isNeedRemoveDUP = true
                println("Aop修改 Class:$mCurClassName 重定向构造函数 MatchEntry:$hookConstructorEntry")
                //压栈
                mConstructorMethodStack.push(new ConstructorMethod(type, true))
            } else {
                if (!mConstructorMethodStack.isEmpty()) {
                    //HOOK构造函数内嵌套的其他构造函数
                    mConstructorMethodStack.push(new ConstructorMethod(type, false))
                }
                super.visitTypeInsn(opcode, type)
            }
        } else {
            super.visitTypeInsn(opcode, type)
        }


    }

    @Override
    void visitInsn(int opcode) {
        if (isNeedRemoveDUP && opcode == Opcodes.DUP) {
            //删除DUP指令
            println("Aop修改 Class:$mCurClassName delete DUP")
            isNeedRemoveDUP = false
            isNeedRecordASTORE = true
        } else {
            isNeedRecordASTORE = false
            super.visitInsn(opcode)
        }
    }

    @Override
    void visitVarInsn(int opcode, int var) {
        if (opcode != Opcodes.ASTORE || !isNeedRecordASTORE) {
            // 非ASTORE指令  如果 不是 NEW、DUP、后紧跟的ASTORE 保持原样
            super.visitVarInsn(opcode, var)
        } else {
            //如果是构造函数区间内  NEW XX ,DUP ... <init>  区间内的 ASTORE hook后需要延后
            ConstructorMethod constructorMethod = mConstructorMethodStack.peek()
            if (constructorMethod.isHook) {
                println("Aop修改 Class:$mCurClassName 构造函数区间内ASTORE")
                OpcodeInfo newOpcodeInfo = new OpcodeInfo(constructorMethod, opcode, var)
                mOpcodeStack.push(newOpcodeInfo)
            } else {
                super.visitVarInsn(opcode, var)
            }
        }

        isNeedRecordASTORE = false


    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        isNeedRecordASTORE = false
        println("修改方法：$owner , $name , $desc")
        GZoomMethodInfo sourceMethod
        GZoomMethodInfo targetMethod

        GZoomDataManager.instance.getData()?.forEach {
            key, value ->
                isMatch = owner == key.targetClassName &&
                        mCurClassName != value.targetClassName &&
                        name == key.targetMethodName &&
                        desc == key.targetMethodDesc

                if (isMatch) {
                    //命中目标
                    sourceMethod = key
                    targetMethod = value
                    return
                }
        }

        if (sourceMethod != null && targetMethod != null) {
            println("Aop修改 Class:$mCurClassName 重定向method:${matchEntry.sourceMethod}")
            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    targetMethod.targetClassName,
                    targetMethod.targetMethodName,
                    targetMethod.targetMethodDesc, false)
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }

        //处理 ASTORE 问题
        if (name == "<init>" && !mConstructorMethodStack.isEmpty()) {
            ConstructorMethod constructorMethod = mConstructorMethodStack.peek()
            if (constructorMethod.className == owner) {
                //
                if (!mOpcodeStack.isEmpty()) {
                    OpcodeInfo opcodeInfo = mOpcodeStack.peek()
                    if (opcodeInfo.method.equals(constructorMethod)) {
                        println("Aop修改 Class:$mCurClassName 重新写入atore ${opcodeInfo.var}")
                        super.visitVarInsn(opcodeInfo.opcode, opcodeInfo.var)
                        //移除操作数
                        mOpcodeStack.pop()
                    }
                    println("Aop修改 Class:$mCurClassName 移除stack ${mConstructorMethodStack.size()} ")
                }

                //移除
                mConstructorMethodStack.pop()
            }

        }
    }

    static class ConstructorMethod {
        String className
        boolean isHook

        ConstructorMethod(String className, boolean isHook) {
            this.className = className
            this.isHook = isHook
        }
    }

    static class OpcodeInfo {
        ConstructorMethod method
        int opcode
        int var

        OpcodeInfo(ConstructorMethod method, int opcode, int var) {
            this.method = method
            this.opcode = opcode
            this.var = var
        }
    }

}