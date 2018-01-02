package com.open.net.server.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :  字符工具类
 */

public final class CfgParser {

    public static HashMap<String,Object> parseToMap(String config_path){
        try {
            return parseToMap(new FileInputStream(config_path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HashMap<String,Object> parseToMap(InputStream mInputStream) {
        HashMap<String,Object> ret = new HashMap<>();
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(mInputStream);
            bufReader = new BufferedReader(inputReader);
            String line;
            HashMap module = null;
            while ((line=bufReader.readLine())!=null) {
                line = line.trim();
                if(line.length() <=0 || line.startsWith("#")){
                    continue;
                }

                Pattern p = Pattern.compile("^\\[(.*)\\]$");
                Matcher m = p.matcher(line);
                if(m.find()){
                    module = new HashMap<>();
                    ret.put(m.group(1),module);
                }else{
                    String[] arr=line.split("=");
                    if(null != module && arr.length>1){
                        String key = arr[0].trim();
                        String val = arr[1].split("#")[0].trim();

                        Pattern val_pattern = Pattern.compile("^\\[(.*)\\]$");
                        Matcher val_matcher = val_pattern.matcher(val);
                        if(val_matcher.find()){
                            module.put(key,val_matcher.group(1).split(","));
                        }else{
                            module.put(key,val);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(null != bufReader){
                try {
                    bufReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(null != inputReader){
                try {
                    inputReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(null != mInputStream){
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    //----------------------------------------------------------------

    public static boolean getBoolean(final HashMap<String,Object> map , final String ... key){
        String val = null;
        HashMap<String,Object> vMap  = map;
        for (int i = 0;i<key.length;i++){
            Object obj = vMap.get(key[i]);
            if(i < key.length -1){
                if(null == obj || !(obj instanceof HashMap)){
                    break;
                }
                vMap = (HashMap)obj;
            }else if(obj instanceof String){
                val = (String)obj;
            }
        }
        if(!TextUtils.isEmpty(val)){
            return Boolean.valueOf(val);
        }
        return false;
    }

    public static int getInt(final HashMap<String,Object> map , final String ... key){
        String val = null;
        HashMap<String,Object> vMap  = map;
        for (int i = 0;i<key.length;i++){
            Object obj = vMap.get(key[i]);
            if(i < key.length -1){
                if(null == obj || !(obj instanceof HashMap)){
                    break;
                }
                vMap = (HashMap)obj;
            }else if(obj instanceof String){
                val = (String)obj;
            }
        }
        if(!TextUtils.isEmpty(val)){
            return Integer.valueOf(val);
        }
        return 0;
    }

    public static long getLong(final HashMap<String,Object> map , final String ... key){
        String val = null;
        HashMap<String,Object> vMap  = map;
        for (int i = 0;i<key.length;i++){
            Object obj = vMap.get(key[i]);
            if(i < key.length -1){
                if(null == obj || !(obj instanceof HashMap)){
                    break;
                }
                vMap = (HashMap)obj;
            }else if(obj instanceof String){
                val = (String)obj;
            }
        }
        if(!TextUtils.isEmpty(val)){
            return Long.valueOf(val);
        }
        return 0;
    }

    public static float getFloat(final HashMap<String,Object> map , final String ... key){
        String val = null;
        HashMap<String,Object> vMap  = map;
        for (int i = 0;i<key.length;i++){
            Object obj = vMap.get(key[i]);
            if(i < key.length -1){
                if(null == obj || !(obj instanceof HashMap)){
                    break;
                }
                vMap = (HashMap)obj;
            }else if(obj instanceof String){
                val = (String)obj;
            }
        }
        if(!TextUtils.isEmpty(val)){
            return Float.valueOf(val);
        }
        return 0;
    }

    public static String getString(final HashMap<String,Object> map , final String ... key){
        String val = null;
        HashMap<String,Object> vMap  = map;
        for (int i = 0;i<key.length;i++){
            Object obj = vMap.get(key[i]);
            if(i < key.length -1){
                if(null == obj || !(obj instanceof HashMap)){
                    break;
                }
                vMap = (HashMap)obj;
            }else if(obj instanceof String){
                val = (String)obj;
            }
        }
        return null == val ? "" : val;
    }

    public static String[] getStringArray(final HashMap<String,Object> map , final String ... key){
        String val[] = null;
        HashMap<String,Object> vMap  = map;
        for (int i = 0;i<key.length;i++){
            Object obj = vMap.get(key[i]);
            if(i < key.length -1){
                if(null == obj || !(obj instanceof HashMap)){
                    break;
                }
                vMap = (HashMap)obj;
            }else if(obj instanceof String[]){
                val = (String[])obj;
            }
        }
        return val;
    }
}
