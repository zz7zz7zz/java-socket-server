package com.open.net.server.impl.udp.bio.processor;

import com.open.net.server.GServer;
import com.open.net.server.message.Message;
import com.open.net.server.object.AbstractClient;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerLog;
import com.open.net.server.pools.MessagePool;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * author       :   long
 * created on   :   2017/12/6
 * description  :   数据读写处理类
 */

public class UdpBioWriteProcessor implements Runnable{

	public static String TAG = "UdpBioWriteProcessor";
	
    public AbstractServerMessageProcessor mMessageProcessor;

    public UdpBioWriteProcessor(AbstractServerMessageProcessor mMessageProcessor) {
        this.mMessageProcessor = mMessageProcessor;
    }

    @Override
    public void run() {
        while(true){

            try {
                writeToClients();

                clearUnreachableMessages();

                mMessageProcessor.onTimeTick();
                
                Thread.sleep(5);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //-------------------------------------------------------------------------------------------
    private void writeToClients() {
        AbstractClient mClient = mMessageProcessor.mWriteMessageQueen.mWriteClientQueen.poll();
        while (null != mClient) {
        	mClient.onWrite();
            mClient = mMessageProcessor.mWriteMessageQueen.mWriteClientQueen.poll();
        } 
    }

    //清除不可达的消息，比如用户关闭连接，此时将发送不出去
    private void clearUnreachableMessages(){
        Iterator<Entry<Long, Message>> iter = mMessageProcessor.mWriteMessageQueen.mMessageMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long,Message> entry = iter.next();
            Message msg = entry.getValue();
            if(msg.mReceivers.isEmpty()){
                iter.remove();
                MessagePool.put(msg);
                ServerLog.getIns().log(TAG, "clearUnreachableMessages A " + msg.msgId);
            }else{
                Iterator<AbstractClient> it = msg.mReceivers.iterator();
                while (it.hasNext()) {
                    AbstractClient mClient = it.next();
                    if(!GServer.isExistClient(mClient)){
                        it.remove();
                    }
                }

                if(msg.mReceivers.isEmpty()) {
                    iter.remove();
                    MessagePool.put(msg);
                    ServerLog.getIns().log(TAG, "clearUnreachableMessages B " + msg.msgId);
                }
            }
        }
    }
}
