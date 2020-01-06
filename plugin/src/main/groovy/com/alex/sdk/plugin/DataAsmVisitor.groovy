package com.alex.sdk.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class DataAsmVisitor extends ClassVisitor {

    ClassVisitor classVisitor
    private String[] interfaces

    DataAsmVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.interfaces = interfaces
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        String nameDesc = name + descriptor
        methodVisitor = new DataAsmMethodChangeVisitor(methodVisitor, access, name,nameDesc, descriptor,interfaces)
        return methodVisitor
    }

}