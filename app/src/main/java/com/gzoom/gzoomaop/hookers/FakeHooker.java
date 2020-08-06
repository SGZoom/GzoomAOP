package com.gzoom.gzoomaop.hookers;

/**
 * Copyright (c) 2020 Tencent. All rights reserved.
 *
 * @author GZoom
 * @date 2020-07-23 21:46
 */
public class FakeHooker {

    public int getResult(int[] datas) {
        int result = 0;
        for(int data : datas) {
            result += data;
        }
        return result;
    }
}
