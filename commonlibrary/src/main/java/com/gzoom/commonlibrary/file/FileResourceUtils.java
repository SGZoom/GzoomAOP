package com.gzoom.commonlibrary.file;

import com.gzoom.commonlibrary.GZoomMethodInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

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
        System.out.println("开始写入临时文件");
        // 先是预替代的方法，后是自己写的替代方法
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8));
        writer.append(source.toString());
        writer.append("=");
        writer.append(target.toString());
        writer.append("\n");
        writer.flush();
    }

    public static HashMap<GZoomMethodInfo, GZoomMethodInfo> readMethodInfoFromFile(String path) {
        HashMap<GZoomMethodInfo, GZoomMethodInfo> map = new HashMap<GZoomMethodInfo, GZoomMethodInfo>();
        if (path == null) {
            return map;
        }
        System.out.println("readMethodInfoFromFile:" + path);
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("文件不存在");
            return map;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null && line.length() > 0) {
                System.out.println("读取到的内容：" + line);
                if (line.contains("=")) {
                    String[] methods = line.split("=");
                    if (methods.length == 2) {
                        GZoomMethodInfo source = GZoomMethodInfo.parseFromString(methods[0]);
                        GZoomMethodInfo target = GZoomMethodInfo.parseFromString(methods[1]);
                        if (source != null && target != null) {
                            map.put(source, target);
                        }
                    }
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
