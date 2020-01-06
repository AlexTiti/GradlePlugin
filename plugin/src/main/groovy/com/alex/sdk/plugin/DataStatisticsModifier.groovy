package com.alex.sdk.plugin

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher


class DataStatisticsModifier {

    private static HashSet<String> hashSet = new HashMap<>()

    static {
        hashSet.add('android.support')
        hashSet.add('com.alex.kotlin.sdk')
    }

    static File modifyClassesFile(File dir, File classFile, File templeFile) {
        File modifyFile = null
        String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
        InputStream inputStream = new FileInputStream(classFile)
        byte[] bytes = modifyClassesInputstream(inputStream)
        if (bytes) {
            modifyFile = new File(templeFile, className.replace(".", "") + '.class')
            if (modifyFile.exists()) {
                modifyFile.delete()
            }
            modifyFile.createNewFile()
            new FileOutputStream(modifyFile).write(bytes)
        }
        return modifyFile
    }

    static File modifyJar(File jarFile, File tempDir, boolean nameHex) {
        /**
         * 读取原 jar
         */
        def file = new JarFile(jarFile, false)

        /**
         * 设置输出到的 jar
         */
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        /**
         * 创建临时缓存目录
         */
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        // 遍历Jar包下的zip文件
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            // 获取每个文件
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = null
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                return null
            }
            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className
                JarEntry jarEntry2 = new JarEntry(entryName)
                jarOutputStream.putNextEntry(jarEntry2)

                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
                if (entryName.endsWith(".class")) {
                    className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "")
                    if (isShouldModify(className)) {
                        modifiedClassBytes = modifyClass(sourceClassBytes)
                    }
                }
                if (modifiedClassBytes == null) {
                    modifiedClassBytes = sourceClassBytes
                }
                jarOutputStream.write(modifiedClassBytes)
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    static byte[] modifyClassesInputstream(InputStream inputStream) {
        ClassReader classReader = new ClassReader(inputStream)
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new DataAsmVisitor(classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    private static String path2ClassName(String className) {
        return className.replace(File.separator, ".").replace('.class', "")
    }

    protected static boolean isShouldModify(String className) {
        Iterator<String> iterator = hashSet.iterator()
        while (iterator.hasNext()) {
            if (className.startsWith(iterator.next())) {
                return false
            }
        }
        if (className.contains('R$')
                || className.contains('R2$')
                || className.contains('R.class')
                || className.contains('R2.class')
                || className.contains('BuildConfig.class')
        ) {
            return false
        }
        return true
    }
}