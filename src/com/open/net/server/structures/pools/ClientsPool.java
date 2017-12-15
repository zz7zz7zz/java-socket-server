package com.open.net.server.structures.pools;

import com.open.net.server.structures.BaseClient;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   客户对象池
 */

public final class ClientsPool {

    private static Class mClientCls;
    private static ConcurrentLinkedQueue<BaseClient> mQueen;

    //初始化
    public static final void init(int connect_max_count, Class cls){
        try {
            mQueen = new ConcurrentLinkedQueue<>();
            mClientCls = cls;
            Constructor<BaseClient> mConstructor = mClientCls.getConstructor();
            for (int i = 0; i< connect_max_count; i++){
                mQueen.add(mConstructor.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //取
    public static final BaseClient get(){
        BaseClient ret= mQueen.poll();
        if(null == ret){
            try {
                Constructor<BaseClient> mConstructor = mClientCls.getConstructor();
                ret = mConstructor.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    //回收
    public static final void put(BaseClient obj){
        if(null != obj){
            obj.reset();
            mQueen.add(obj);
        }
    }
}
