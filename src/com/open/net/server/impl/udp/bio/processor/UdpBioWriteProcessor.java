package com.open.net.server.impl.udp.bio.processor;

import com.open.net.server.GServer;
import com.open.net.server.structures.AbstractClient;
import com.open.net.server.structures.AbstractMessageProcessor;
import com.open.net.server.structures.ServerLog;
import com.open.net.server.structures.message.Message;
import com.open.net.server.structures.pools.MessagePool;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * author       :   long
 * created on   :   2017/12/6
 * description  :   数据读写处理类
 */

public class UdpBioWriteProcessor implements Runnable{

	public static String TAG = "UdpBioWriteProcessor";
	
    public AbstractMessageProcessor mMessageProcessor;

    public UdpBioWriteProcessor(AbstractMessageProcessor mMessageProcessor) {
        this.mMessageProcessor = mMessageProcessor;
    }

    @Override
    public void run() {
        while(true){

            try {

                writeToClients();

                clearUnreachableMessages();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //-------------------------------------------------------------------------------------------
    private void writeToClients() throws IOException {
        for (AbstractClient mClient: mMessageProcessor.mWriteMessageQueen.mWriteClientSet) {
            if(!mClient.onWrite()){
                mClient.onClose();
            }
        }
        mMessageProcessor.mWriteMessageQueen.mWriteClientSet.clear();
    }

    //清除不可达的消息，比如用户关闭连接，此时将发送不出去
    private void clearUnreachableMessages(){
        Iterator iter = mMessageProcessor.mWriteMessageQueen.mMessageMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long,Message> entry = (Map.Entry) iter.next();
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
