package com.alex.sdk.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class DataAsmTransform extends Transform {

    private Project project

    DataAsmTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "DataAutoTrackDataTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        printRight()
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 获取输出文件目录
                def outputFile = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                HashMap<String, File> hashMap = new HashMap<>()
                File dir = directoryInput.file
                dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File classFile ->
                    if (DataStatisticsModifier.isShouldModify(classFile.name)) {
                        File modifyFile = DataStatisticsModifier.modifyClassesFile(dir, classFile, context.getTemporaryDir())
                        if (modifyFile) {
                            String key = classFile.absolutePath.replace(dir.absolutePath, "")
                            hashMap.put(key, modifyFile) // 保存ASM修改后的文件
                        }
                    }
                }
                // copy 原始所有文件
                FileUtils.copyDirectory(directoryInput.file, outputFile)
                hashMap.entrySet().each { Map.Entry<String, File> entry ->
                    File target = new File(outputFile.absolutePath + entry.key)
                    if (target.exists()) {
                        target.delete()
                    }
                    FileUtils.copyDirectory(entry.value, target)
                    entry.value.delete()
                }
            }
            printRight()

            /**遍历 jar*/
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.file.name
                /**截取文件路径的 md5 值重命名输出文件,因为可能同名,会覆盖*/
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                /** 获取 jar 名字*/
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }
                /** 获得输出文件*/
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                File modifiedJar = null
                modifiedJar = DataStatisticsModifier.modifyJar(jarInput.file, context.getTemporaryDir(), true)
                if (modifiedJar == null) {
                    modifiedJar = jarInput.file
                }
                FileUtils.copyFile(modifiedJar, dest)
            }
        }
        printRight()
    }

    static void printRight() {
        println()
        println("***********************************")
        println("********** DataAsmTransform **********")
        println("***********************************")
        println()
    }
}