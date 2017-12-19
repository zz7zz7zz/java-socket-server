package com.open.net.server.impl.tcp.nio.processor;

import com.open.net.server.GServer;
import com.open.net.server.impl.tcp.nio.NioClient;
import com.open.net.server.structures.AbstractClient;
import com.open.net.server.structures.AbstractMessageProcessor;
import com.open.net.server.structures.ServerLog;
import com.open.net.server.structures.message.Message;
import com.open.net.server.structures.pools.MessagePool;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   数据读写处理类
 */

public final class NioReadWriteProcessor implements Runnable {

	public static String TAG = "NioReadWriteProcessor";
	
    private ConcurrentLinkedQueue<NioClient> mAcceptClientQueen;
    private AbstractMessageProcessor mMessageProcessor;

    private Selector            mReadSelector;
    private Selector            mWriteSelector;

    public NioReadWriteProcessor(ConcurrentLinkedQueue<NioClient> mAcceptClientQueen, AbstractMessageProcessor mMessageProcessor) throws IOException {
        this.mAcceptClientQueen = mAcceptClientQueen;
        this.mMessageProcessor  = mMessageProcessor;
        this.mReadSelector      = Selector.open();
        this.mWriteSelector     = Selector.open();
    }

    @Override
    public void run() {

        while(true){

            try {

                acceptNewClients();

                readFromClients();

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
    private void acceptNewClients() throws IOException {
        NioClient mClient = mAcceptClientQueen.poll();
        while(null != mClient){
            mClient.mSocketChannel.configureBlocking(false);
            SelectionKey key = mClient.mSocketChannel.register(mReadSelector, SelectionKey.OP_READ);
            key.attach(mClient);
            mClient = mAcceptClientQueen.poll();
        }
    }

    //-------------------------------------------------------------------------------------------
    private void readFromClients() throws IOException {
        int readReady = mReadSelector.selectNow();
        if(readReady > 0){
            Set<SelectionKey> selectedKeys = this.mReadSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
//System.out.println(" isValid " + key.isValid() +" isAcceptable " + key.isAcceptable() + " isConnectable " + key.isConnectable()+ " isWritable " + key.isWritable() + " isReadable " + key.isReadable() );
                if(!key.isValid()){
                    continue;
                }

                readFromClients(key);
            }

            selectedKeys.clear();
        }
    }

    private void readFromClients(SelectionKey key) {
        NioClient mClient = (NioClient) key.attachment();
        if(!mClient.onRead()){
            ServerLog.getIns().log(TAG, "client "+ mClient.mClientId +" close when read ");  
            try {
                key.attach(null);
                key.cancel();
                key.channel().close();
            }catch (Exception e2){
                e2.printStackTrace();
            }

            mClient.onClose();
        }
    }

    //-------------------------------------------------------------------------------------------
    private void writeToClients() throws IOException {
        registerWriteOpt();
        write();
    }

    private void registerWriteOpt() throws ClosedChannelException {
        for (AbstractClient mClient: mMessageProcessor.mWriteMessageQueen.mWriteClientSet) {
            if(null != mClient){
                ((NioClient)mClient).mSocketChannel.register(this.mWriteSelector, SelectionKey.OP_WRITE,mClient);
            }
        }
        mMessageProcessor.mWriteMessageQueen.mWriteClientSet.clear();
    }

    private void write() throws IOException {
        // Select from the Selector.
        int writeReady = this.mWriteSelector.selectNow();

        if(writeReady > 0){
            Set<SelectionKey>      selectionKeys = this.mWriteSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator   = selectionKeys.iterator();

            while(keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if(!key.isValid()){
                    continue;
                }

                write(key);
            }

            selectionKeys.clear();
        }
    }

    private void write(SelectionKey key){
        NioClient mClient = (NioClient) key.attachment();
        if(mClient.onWrite()){
            key.cancel();
        }else {
            key.attach(null);
            key.cancel();
            try {
                key.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mClient.onClose();
        }
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
