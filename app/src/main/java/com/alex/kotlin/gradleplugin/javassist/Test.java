package com.alex.kotlin.gradleplugin.javassist;

import javassist.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Alex
 * @date 2020-01-02 16:13
 * @email 18238818283@sina.cn
 * @desc ...
 */

public class Test {

    public static void main(String args[]) throws NotFoundException, CannotCompileException,
            IOException, ClassNotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("com.alex.kotlin.gradleplugin.javassist.UserAction");
        // 在构造函数中插入语句
        CtConstructor ctConstructor = ctClass.getDeclaredConstructors()[0];
        ctConstructor.insertBefore("System.out.println(\"Create object of userAction !\");");
        // 在执行方法中埋点统计方法耗时
        CtMethod ctMethod = ctClass.getDeclaredMethod("action");
        ctMethod.insertBefore("System.out.println(\" " + ctMethod.getName() + "统计开始\");");
        ctMethod.insertAfter("System.out.println(\" " + ctMethod.getName() + "统计结束\");");
        // 设置继承接口
        CtClass superClass = classPool.get("java.lang.Cloneable");
        ctClass.setInterfaces(new CtClass[]{superClass});
        // 添加属性Field,需要声明属性，第二参数表示要添加的Class
        CtField ctField = CtField.make("int number;", ctClass);
        // 第二个参数表示初始化number的值
        ctField.setModifiers(Modifier.PUBLIC);
        ctClass.addField(ctField, CtField.Initializer.constant(10));
        // 添加方法
        CtMethod ctMethod1 = CtMethod.make("public void addInt(int add){add + add;}", ctClass);
        ctMethod1.setModifiers(Modifier.PRIVATE);
        ctClass.addMethod(ctMethod1);
        // 创建类
        CtClass newClass = classPool.makeClass("Point");
        CtMethod ctMethod2 = CtMethod.make("public void addInt(int add){add + add;}", newClass);
        ctMethod2.setModifiers(Modifier.PRIVATE);
        newClass.addMethod(ctMethod2);
        newClass.writeFile("/Users/wuliangliang/OpenSourceStudyProject/GradlePlugin/app/src/main/java/class");
        // 创建接口
        CtClass interfaceClass = classPool.makeInterface("PointInterface");
        CtMethod abstractMethod = CtNewMethod.abstractMethod(CtClass.intType, "addInt", new CtClass[]{CtClass.intType, CtClass.intType}, null, interfaceClass);
        interfaceClass.addMethod(abstractMethod);
        interfaceClass.writeFile("/Users/wuliangliang/OpenSourceStudyProject/GradlePlugin/app/src/main/java/class");

        ctClass.writeFile("/Users/wuliangliang/OpenSourceStudyProject/GradlePlugin/app/src/main/java/class");
        Class<?> userActionClass = ctClass.toClass();
        try {
            Object userAction = userActionClass.newInstance();
            Method method = userActionClass.getDeclaredMethod("action", String.class);
            method.setAccessible(true);
            method.invoke(userAction, "");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }
}