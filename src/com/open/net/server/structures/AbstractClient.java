package com.open.net.server.structures;

import com.open.net.server.GServer;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   客户连接对象
 */

public abstract class AbstractClient {

    private static long G_AUTO_INCREAMEN_CLIENT_ID = 0;

    public long     mClientId = 0;
    public String   mHost = null;
    public int      mPort = -1;
    public Object   mAttachment;
    protected ConcurrentLinkedQueue<Long> mReadMessageIds = new ConcurrentLinkedQueue<Long>();
    protected ConcurrentLinkedQueue<Long> mWriteMessageIds = new ConcurrentLinkedQueue<Long>();
    protected AbstractMessageProcessor mMessageProcessor;

    //--------------------------------------------------------------------------------------
    public void init(String mHost, int mPort ,AbstractMessageProcessor mMessageProcessor) {
        G_AUTO_INCREAMEN_CLIENT_ID++;
        mClientId = G_AUTO_INCREAMEN_CLIENT_ID;
        this.mHost = mHost;
        this.mPort = mPort;
        this.mMessageProcessor = mMessageProcessor;
        GServer.register(this);
    }

    public void onClose(){
    	
    	//退出客户端的时候需要把要写给该客户端的数据清空
        Long mMessageId = pollWriteMessageId();
        while (null != mMessageId) {
            mMessageProcessor.mWriteMessageQueen.remove(this, mMessageId);
            mMessageId = pollWriteMessageId();
        }
        
        GServer.unregister(this);
    }

    public void reset(){
        mClientId = 0 ;
        mHost = null;
        mPort = -1;
        mAttachment = null;
        mReadMessageIds.clear();
        mWriteMessageIds.clear();
        mMessageProcessor = null;
    }
    //--------------------------------------------------------------------------------------

    public abstract boolean onRead();

    public abstract boolean onWrite();

    //--------------------------------------------------------------------------------------
    public void addReadMessageId(long id) {
        mReadMessageIds.add(id);
    }

    public Long pollReadMessageId(){
        return mReadMessageIds.poll();
    }

    public void removeReadMessageId(long id){
        mReadMessageIds.remove(id);
    }

    //--------------------------------------------------------------------------------------
    public void addWriteMessageId(long id) {
        mWriteMessageIds.add(id);
    }

    public Long pollWriteMessageId(){
        return mWriteMessageIds.poll();
    }

    public void removeWriteMessageId(long id){
        mWriteMessageIds.remove(id);
    }

    //--------------------------------------------------------------------------------------
    public final void attach(Object mAttachment){
        this.mAttachment = mAttachment;
    }

    public final Object getAttachment(){
        return mAttachment;
    }

    //--------------------------------------------------------------------------------------

}
