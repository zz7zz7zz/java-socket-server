package com.open.net.server.structures;

import com.open.net.server.GServer;
import com.open.net.server.structures.message.Message;
import com.open.net.server.structures.message.MessageReadQueen;
import com.open.net.server.structures.message.MessageWriteQueen;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   信息处理类
 */

public abstract class BaseMessageProcessor {

    public MessageReadQueen mReadMessageQueen   = new MessageReadQueen();
    public MessageWriteQueen mWriteMessageQueen  = new MessageWriteQueen();

    //-------------------------------------------------------------------------------------------
    //单播
    private final void unicast(BaseClient client, Message msg){
        if(null != client){
            mWriteMessageQueen.put(client,msg);
        }
    }

    //多播(组播)
    private final void multicast(BaseClient[] clients, Message msg){
        for (BaseClient client:clients) {
            if(null != client){
                mWriteMessageQueen.put(client,msg);
            }
        }
    }

    //广播
    private final void broadcast(Message msg){
        for (BaseClient client: GServer.getClients().values()) {
            if(null != client){
                mWriteMessageQueen.put(client,msg);
            }
        }
    }
    //-------------------------------------------------------------------------------------------
    //单播
    public void unicast(BaseClient client, byte[] src , int offset , int length){
        if(null != client){
            Message msg = mWriteMessageQueen.build(src,offset,length);
            unicast(client,msg);
        }
    }

    //多播(组播)
    public void multicast(BaseClient []clients, byte[] src , int offset , int length){
        if(null != clients && clients.length > 0){
            Message msg = mWriteMessageQueen.build(src,offset,length);
            multicast(clients,msg);
        }
    }

    //广播
    public void broadcast(byte[] src , int offset , int length){
        Message msg = mWriteMessageQueen.build(src,offset,length);
        broadcast(msg);
    }
    //-------------------------------------------------------------------------------------------

    public final void onReceiveData(BaseClient client, byte[] src , int offset , int length) {
        Message msg = mReadMessageQueen.build(src,offset,length);
        mReadMessageQueen.put(client,msg);
    }

    public final void onReceiveMessages(BaseClient mClient){
        Long msgId = mClient.pollReadMessageId();
        while (null != msgId){
            Message msg = mReadMessageQueen.mMessageMap.get(msgId);
            if(null == msg){
                msgId = mClient.pollReadMessageId();
                continue;
            }

            onReceiveMessage(mClient,msg);
            mReadMessageQueen.remove(mClient,msg);

            msgId = mClient.pollReadMessageId();
        }
    }
    //-------------------------------------------------------------------------------------------
    public abstract void onReceiveMessage(BaseClient client, Message msg);


}
