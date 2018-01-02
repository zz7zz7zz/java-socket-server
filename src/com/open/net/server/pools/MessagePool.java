package com.open.net.server.pools;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.open.net.server.message.Message;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   消息池
 */

public final class MessagePool {

    public static ConcurrentLinkedQueue<Message> mQueen = new ConcurrentLinkedQueue<Message>();

    //初始化
    public static final void init(int msg_max_size){
        for (int i = 0;i< msg_max_size;i++){
            mQueen.add(new Message());
        }
    }

    //取
    public static final Message get(){
        Message ret = mQueen.poll();
        if(null == ret){
            ret = new Message();
        }
        return ret;
    }

    //回收
    public static final void put(Message obj){
        if(null != obj){
            obj.reset();
            mQueen.add(obj);
        }
    }
}
