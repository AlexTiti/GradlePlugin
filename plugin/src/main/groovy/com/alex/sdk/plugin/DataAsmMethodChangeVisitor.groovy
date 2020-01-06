package com.alex.sdk.plugin


import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class DataAsmMethodChangeVisitor extends AdviceAdapter {

    private static final String SDK_API_CLASS = "com/alex/kotlin/sdk/DataStatisticsApi"
    private String[] interfaces
    private String nameDesc
    private MethodVisitor methodVisitor

    protected DataAsmMethodChangeVisitor(MethodVisitor methodVisitor, int access, String name, String nameDesc, String descriptor, String[] interfaces) {
        super(Opcodes.ASM6, methodVisitor, access, name, descriptor)
        this.interfaces = interfaces
        this.methodVisitor = methodVisitor
        this.nameDesc = nameDesc
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode)
        if (interfaces == null || interfaces.length == 0) {
            return
        }
        if (interfaces.contains('android/view/View$OnClickListener')
                && nameDesc == 'onClick(Landroid/view/View;)V'
                && methodDesc == '((Landroid/view/View;)V)') {
            methodVisitor.visitInsn(ALOAD, 1)
            methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "staticTraceViewOnClick", "(Landroid/view/View;)V", false)
        }
    }
}