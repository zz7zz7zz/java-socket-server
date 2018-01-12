package com.open.net.server.impl.tcp.nio;

import com.open.net.server.impl.tcp.nio.processor.NioAcceptProcessor;
import com.open.net.server.impl.tcp.nio.processor.NioReadWriteProcessor;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerConfig;
import com.open.net.server.object.ServerLock;
import com.open.net.server.object.ServerLog;
import com.open.net.server.object.ServerLog.LogListener;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   服务器对象
 */

public final class NioServer {

    private ServerLock mServerLock;
    private NioAcceptProcessor    mNioAcceptProcessor;
    private NioReadWriteProcessor mNioReadWriteProcessor;
    private ConcurrentLinkedQueue<NioClient>    mClientQueen  = new ConcurrentLinkedQueue<>();

    public NioServer(ServerConfig mServerInfo, AbstractServerMessageProcessor mMessageProcessor,LogListener mLogListener) throws IOException {
        this.mServerLock = new ServerLock();
        this.mNioAcceptProcessor = new NioAcceptProcessor(mServerInfo,mServerLock,mClientQueen,mMessageProcessor);
        this.mNioReadWriteProcessor     = new NioReadWriteProcessor(mClientQueen,mMessageProcessor);
        ServerLog.getIns().setLogListener(mLogListener);
    }

    public void start(){

        Thread mNioAcceptProcessorThread   = new Thread(this.mNioAcceptProcessor);
        Thread mNioReadWriteProcessorThread       = new Thread(this.mNioReadWriteProcessor);

        mNioAcceptProcessorThread.setName("Nio-Accept-Thread");
        mNioReadWriteProcessorThread.setName("Nio-ReadWrite-Thread");
        
        mNioAcceptProcessorThread.start();
        mNioReadWriteProcessorThread.start();

        mServerLock.waitEnding();
    }
}
