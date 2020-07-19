package com.gzoom.commonlibrary.file;

import com.gzoom.commonlibrary.GZoomMethodInfo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Copyright (c) 2020 Tencent. All rights reserved.
 *
 * @author gzoom
 * @date 2020-07-19 17:40
 */
public class FileResourceUtils {
    private static final String S_DEFAULT_PATH = "META-INF/GZoomAop-data";


    public static String getMethodPath() {
        return S_DEFAULT_PATH;
    }

    public static void writeMethod(GZoomMethodInfo target, GZoomMethodInfo source, OutputStream outputStream)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8));
        writer.append(source.toString());
        writer.append("=");
        writer.append(target.toString());
        writer.append("\n");
        writer.flush();
    }
}
