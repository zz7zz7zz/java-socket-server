package com.open.net.server.message;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.open.net.server.object.AbstractServerClient;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   写队列
 */

public class MessageWriteQueen {

    private MessageBuffer   mWriteMessageBuffer = MessageBuffer.getInstance();
    public HashMap<Long,Message> mMessageMap = new HashMap<>(1024);//真正的消息队列

    public ConcurrentLinkedQueue<AbstractServerClient> mWriteClientQueen = new ConcurrentLinkedQueue<AbstractServerClient>();//有需要发送消息的客户端

    public Message build(byte[] src , int offset , int length){
        Message msg = mWriteMessageBuffer.build(src,offset,length);
        return msg;
    }

    public void put(AbstractServerClient client,Message msg){
        //1.消息进入消息池
        //2.每个客户端存入消息引用
        //3.每个消息添加要发送的对象
        //4.哪些客户端需要写入数据
        mMessageMap.put(msg.msgId,msg);

        client.addWriteMessageId(msg.msgId);

        msg.mReceivers.add(client);

        mWriteClientQueen.add(client);
    }

    public void remove(AbstractServerClient mClient,Message msg){

        mClient.removeWriteMessageId(msg.msgId);

        msg.mReceivers.remove(mClient);

        if(msg.mReceivers.isEmpty()){
            mMessageMap.remove(msg.msgId);
            mWriteMessageBuffer.release(msg);
        }
    }

    public void remove(AbstractServerClient mClient,long msgId){
        Message msg = mMessageMap.get(msgId);
        if(null != msg){
            remove(mClient,msg);
        }
    }
}
