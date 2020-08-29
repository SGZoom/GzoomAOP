package com.gzoom.aopplugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.google.common.collect.ImmutableSet
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import org.apache.commons.io.IOUtils
import org.gradle.internal.impldep.org.objectweb.asm.Opcodes
import org.objectweb.asm.ClassVisitor
import org.apache.commons.io.FileUtils
import com.android.build.api.transform.Format

import javax.xml.crypto.dsig.TransformException
import java.util.concurrent.Semaphore
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.io.File

import static com.android.build.api.transform.QualifiedContent.DefaultContentType.CLASSES

class GZoomTransformation extends Transform {
    private static final String NAME = "GZoomTransformation"

    /**
     *
     */
    private WaitableExecutor mExecutor = WaitableExecutor.useGlobalSharedThreadPool()

    /**
     * 并发量，控制处理线程数
     */
    private static Semaphore mSemaphore = new Semaphore(8)

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        println('gzoom - start prepare')
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        // 需要区分增量，输入不同
        boolean isIncremental = transformInvocation.isIncremental()
        // 非增量需要清空
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        // 输入源有两级
        transformInvocation.getInputs().forEach {
            it.directoryInputs.forEach {
                directoryInput ->
                    handleDirectories(outputProvider, directoryInput, isIncremental)
            }

            it.jarInputs.forEach {
                jarInput ->
                    handleJarInputs(outputProvider, jarInput, isIncremental)
            }
        }


        mExecutor.waitForAllTasks()
    }

    private void handleJarInputs(TransformOutputProvider outputProvider, JarInput jarInput, boolean isIncremental) throws IOException {
        Status status = jarInput.getStatus()
        File dest = outputProvider.getContentLocation(
                jarInput.getName(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR)
        if (isIncremental) {
            if (status == Status.ADDED || status == Status.CHANGED) {
                transformJarFile(jarInput.getFile(), dest)
            } else if (status == Status.REMOVED && dest.exists()) {
                FileUtils.forceDelete(dest)
            }
        } else {
            transformJarFile(jarInput.getFile(), dest)
        }
    }

    private void handleDirectories(TransformOutputProvider outputProvider, DirectoryInput directoryInput, boolean isIncremental) {
        if (directoryInput == null || outputProvider == null) {
            return
        }
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
                directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY)
        try {
            FileUtils.forceMkdir(dest)
            String srcDirPath = directoryInput.getFile().getPath()
            String destDirPath = dest.getPath()
            if (isIncremental) {
                directoryInput.changedFiles.each {
                    file, state ->
                        String destFilePath = file.getPath().replace(srcDirPath, destDirPath)
                        File destFile = new File(destFilePath)
                        switch (status) {
                            case Status.NOTCHANGED:
                                break
                            case Status.REMOVED:
                                FileUtils.forceDelete(destFile)
                                break
                            case Status.ADDED:
                            case Status.CHANGED:
                                if (file.isFile() && file.getPath().endsWith(".class")) {
                                    transformClassFile(file, destFile)
                                } else if (file.isFile()) {
                                    FileUtils.copyFile(file, destFile)
                                }
                                break
                        }
                }

            } else {
                File file = directoryInput.getFile()
                // eachFileRecurse 递归该目录下的所有文件
                eachFileResource(file, srcDirPath, destDirPath)
            }
        } catch (IOException e) {
            e.printStackTrace()
        }

    }

    private void eachFileResource(File file, String srcDirPath, String destDirPath) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles()
            for (File each : files) {
                eachFileResource(each, srcDirPath, destDirPath)
            }
        }
        String destFilePath = file.getPath().replace(srcDirPath, destDirPath)
        File destFile = new File(destFilePath)
        if (destFile.isFile() && file.getPath().endsWith(".class")) {
            transformClassFile(file, destFile)
        } else if (file.isFile()) {
            FileUtils.copyFile(file, destFile)
        }
    }

    private void transformClassFile(File inputClassFile, File destFile) {
        println("transform ClassFile classFile：$inputClassFile.path  dest: $destFile.path")
        mExecutor.execute({
            mSemaphore.acquire()
            FileUtils.touch(destFile)
            FileInputStream fis = new FileInputStream(inputClassFile)
            ClassReader classReader = new ClassReader(fis)
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            ClassVisitor cv = new GZoomAopClassVisitor(Opcodes.ASM5, classWriter)
            classReader.accept(cv, org.objectweb.asm.ClassReader.EXPAND_FRAMES)
            FileOutputStream outputStream = new FileOutputStream(destFile)
            outputStream.write(classWriter.toByteArray())
            IOUtils.closeQuietly(outputStream)
            IOUtils.closeQuietly(fis)
            mSemaphore.release()
            return null
        })
    }

    private void transformJarFile(File jarFile, File desc) {
        println("transform Jar jarFile：" + jarFile.getPath() + "  desc:" + desc.getPath())
        mExecutor.execute(
                {
                    mSemaphore.acquire()
                    FileUtils.touch(desc)
                    JarFile jf = new JarFile(jarFile)
                    Enumeration<JarEntry> je = jf.entries()
                    JarOutputStream jos = new JarOutputStream(new FileOutputStream(desc))
                    while (je.hasMoreElements()) {
                        JarEntry jarEntry = je.nextElement()
                        ZipEntry zipEntry = new ZipEntry(jarEntry.getName())
                        InputStream originIns = jf.getInputStream(jarEntry)
                        byte[] classBytes = toByteArray(originIns)
                        if (jarEntry.getName().endsWith(".class")) {
                            org.objectweb.asm.ClassReader classReader = new org.objectweb.asm.ClassReader(classBytes)
                            org.objectweb.asm.ClassWriter classWriter = new org.objectweb.asm.ClassWriter(classReader, org.objectweb.asm.ClassWriter.COMPUTE_MAXS)
                            ClassVisitor cv = new GZoomAopClassVisitor(org.objectweb.asm.Opcodes.ASM5, classWriter)
                            classReader.accept(cv, org.objectweb.asm.ClassReader.EXPAND_FRAMES)
                            classBytes = classWriter.toByteArray()
                        }

                        jos.putNextEntry(zipEntry)
                        jos.write(classBytes)
                        jos.closeEntry()

                    }
                    IOUtils.closeQuietly(jos)
                    IOUtils.closeQuietly(jf)
                    mSemaphore.release()
                    return null
                }
        )

    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        // 处理class
        return ImmutableSet.of(CLASSES)
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        // 所有的字节码（测试代码也会受影响哦）
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    static byte[] toByteArray(final InputStream input) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream()
        final byte[] buffer = new byte[8024]
        int n
        long count = 0
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n)
            count += n
        }
        return output.toByteArray()
    }
}