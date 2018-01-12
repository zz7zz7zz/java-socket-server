package com.open.net.server.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :  字符工具类
 */
public final class ExceptionUtil {
	
	public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
          return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
          if (t instanceof UnknownHostException) {
            return "";
          }
          t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
