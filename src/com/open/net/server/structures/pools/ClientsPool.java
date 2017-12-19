package com.open.net.server.structures.pools;

import com.open.net.server.structures.AbstractClient;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   客户对象池
 */

public final class ClientsPool {

    private static Class<? extends AbstractClient> mClientCls;
    private static ConcurrentLinkedQueue<AbstractClient> mQueen;

    //初始化
    public static final void init(int connect_max_count, Class<? extends AbstractClient> cls){
        try {
            mQueen = new ConcurrentLinkedQueue<>();
            mClientCls = cls;
            Constructor<AbstractClient> mConstructor = (Constructor<AbstractClient>) mClientCls.getConstructor();
            for (int i = 0; i< connect_max_count; i++){
                mQueen.add(mConstructor.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //取
    public static final AbstractClient get(){
        AbstractClient ret= mQueen.poll();
        if(null == ret){
            try {
                Constructor<AbstractClient> mConstructor = (Constructor<AbstractClient>) mClientCls.getConstructor();
                ret = mConstructor.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    //回收
    public static final void put(AbstractClient obj){
        if(null != obj){
            obj.reset();
            mQueen.add(obj);
        }
    }
}
