package com.open.net.server.message;

import java.util.HashMap;

import com.open.net.server.object.AbstractServerClient;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   读队列
 */

public final class MessageReadQueen {

    public MessageBuffer   mReadMessageBuffer  = MessageBuffer.getInstance();
    public HashMap<Long,Message> mMessageMap = new HashMap<>(1024);//真正的消息队列

    public Message build(byte[] src , int offset , int length){
        Message msg = mReadMessageBuffer.build(src,offset,length);
        return msg;
    }

    public void put(AbstractServerClient client, Message msg){
        mMessageMap.put(msg.msgId,msg);
        client.addReadMessageId(msg.msgId);
    }

    public void remove(AbstractServerClient mClient, Message msg){
        mMessageMap.remove(msg.msgId);
        mClient.removeReadMessageId(msg.msgId);

        mReadMessageBuffer.release(msg);
    }
}
