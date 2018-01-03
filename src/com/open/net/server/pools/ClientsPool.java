package com.open.net.server.pools;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.open.net.server.object.AbstractServerClient;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   客户对象池
 */

public final class ClientsPool {

    private static Class<? extends AbstractServerClient> mClientCls;
    private static ConcurrentLinkedQueue<AbstractServerClient> mQueen;

    //初始化
    public static final void init(int connect_max_count, Class<? extends AbstractServerClient> cls){
        try {
            mQueen = new ConcurrentLinkedQueue<>();
            mClientCls = cls;
            Constructor<AbstractServerClient> mConstructor = (Constructor<AbstractServerClient>) mClientCls.getConstructor();
            for (int i = 0; i< connect_max_count; i++){
                mQueen.add(mConstructor.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //取
    public static final AbstractServerClient get(){
        AbstractServerClient ret= mQueen.poll();
        if(null == ret){
            try {
                Constructor<AbstractServerClient> mConstructor = (Constructor<AbstractServerClient>) mClientCls.getConstructor();
                ret = mConstructor.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    //回收
    public static final void put(AbstractServerClient obj){
        if(null != obj){
            obj.reset();
            mQueen.add(obj);
        }
    }
}
