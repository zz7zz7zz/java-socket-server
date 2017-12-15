package com.open.net.server.utils;

/**
 * Created by long on 2017/12/6.
 */

public final class KeyUtil {

    public static String getKey(String mHost, int mPort){
        return mHost + ":"+ mPort;
    }
}
