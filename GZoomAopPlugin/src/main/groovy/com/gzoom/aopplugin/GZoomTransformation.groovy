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
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassVisitor

import javax.xml.crypto.dsig.TransformException
import java.util.concurrent.Semaphore
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

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
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        System.out.println("gzoom - start prepare")
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        // 需要区分增量，输入不同
        boolean isIncremental = transformInvocation.isIncremental()
        if (isIncremental) {
            outputProvider.deleteAll()
        }
        // 输入源有两级
        Collection<TransformInput> inputCollections = transformInvocation.getInputs()
        for (TransformInput input : inputCollections) {
            Collection<DirectoryInput> directories = input.getDirectoryInputs()
            for (DirectoryInput directoryInput : directories) {
                System.out.println("gzoom - start prepare:" + directoryInput.getName())
                handleDirectories(outputProvider, directoryInput, isIncremental)
            }
//            Collection<JarInput> jars = input.getJarInputs()
//            for (JarInput jarInput : jars) {
//                System.out.println("gzoom - start prepare:" + jarInput.getName())
//                handleJarInputs(outputProvider, jarInput, isIncremental)
//            }
        }
    }

    private void handleJarInputs(TransformOutputProvider outputProvider, JarInput jarInput, boolean isIncremental) throws IOException {
        Status status = jarInput.getStatus()
        File dest = outputProvider.getContentLocation(
                jarInput.getName(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                com.android.build.api.transform.Format.JAR)
        if (isIncremental) {
            if (status == Status.ADDED || status == Status.CHANGED) {
                transformJarFile(jarInput.getFile(), dest)
            } else if (status == Status.REMOVED && dest.exists()) {
                org.apache.commons.io.FileUtils.forceDelete(dest)
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
                com.android.build.api.transform.Format.DIRECTORY)
        try {
            org.apache.commons.io.FileUtils.forceMkdir(dest)
            String srcDirPath = directoryInput.getFile().getAbsolutePath()
            String destDirPath = dest.getAbsolutePath()
            if (isIncremental) {
                Map<File, Status> map = directoryInput.getChangedFiles()
                for (File file : map.keySet()) {
                    String destFilePath = file.getAbsolutePath().replace(srcDirPath, destDirPath)
                    File destFile = new File(destFilePath)
                    Status status = map.get(file)
                    if (status == Status.REMOVED) {
                        org.apache.commons.io.FileUtils.forceDelete(destFile)
                    } else if (status == Status.ADDED || status == Status.CHANGED) {
                        if (file.isFile() || file.getPath().endsWith(".class")) {
                            // 我们的目标文件，需要修改
                            transformClassFile(file, destFile)
                        } else if (file.isFile()) {
                            org.apache.commons.io.FileUtils.copyFile(file, destFile)
                        }
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
        String destFilePath = file.getAbsolutePath().replace(srcDirPath, destDirPath)
        File destFile = new File(destFilePath)
        if (destFile.isFile() && file.getPath().endsWith(".class")) {
            transformClassFile(file, destFile)
        } else if (file.isFile()) {
            org.apache.commons.io.FileUtils.copyFile(file, destFile)
        }
    }

    private void transformClassFile(File inputClassFile, File destFile) {
        System.out.println("transform ClassFile classFile：$inputClassFile.getPath()  dest:" + destFile.getPath())
        mExecutor.execute( {
            mSemaphore.acquire()
            org.apache.commons.io.FileUtils.touch(destFile)
            FileInputStream fis = new FileInputStream(inputClassFile)
            ClassReader classReader = new ClassReader(fis)
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            ClassVisitor cv = new GZoomAopClassVisitor(Opcodes.ASM5, classWriter)
            classReader.accept(cv, EXPAND_FRAMES)
            FileOutputStream outputStream = new FileOutputStream(destFile)
            outputStream.write(classWriter.toByteArray())
            IOUtils.closeQuietly(outputStream)
            IOUtils.closeQuietly(fis)
            mSemaphore.release()
            return null
        })
    }

    private void transformJarFile(File jarFile, File desc) {
        System.out.println("transform Jar jarFile：" + jarFile.getPath() + "  desc:" + desc.getPath())
        mExecutor.execute({
            mSemaphore.acquire()
            org.apache.commons.io.FileUtils.touch(desc)
            JarFile jf = new JarFile(jarFile)
            Enumeration<JarEntry> je = jf.entries()
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(desc))
            while (je.hasMoreElements()) {
                JarEntry jarEntry = je.nextElement()
                ZipEntry zipEntry = new ZipEntry(jarEntry.getName())
                InputStream originIns = jf.getInputStream(jarEntry)
                byte[] classBytes = toByteArray(originIns)
                if (jarEntry.getName().endsWith(".class")) {
                    ClassReader classReader = new ClassReader(classBytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new GZoomAopClassVisitor(Opcodes.ASM5, classWriter)
                    classReader.accept(cv, EXPAND_FRAMES)
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
        })
    }

    @Override
    public String getName() {
        return NAME
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        // 处理class
        return TransformManager.CONTENT_CLASS
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        // 所有的字节码（测试代码也会受影响哦）
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    public boolean isIncremental() {
        return true
    }

    public byte[] toByteArray(final InputStream input) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream()
        final byte[] buffer = new byte[8024]
        int n = 0
        long count = 0
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n)
            count += n
        }
        return output.toByteArray()
    }
}