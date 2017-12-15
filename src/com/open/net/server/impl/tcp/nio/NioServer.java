package com.open.net.server.impl.tcp.nio;

import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;
import com.open.net.server.impl.tcp.nio.processor.SocketAcceptProcessor;
import com.open.net.server.impl.tcp.nio.processor.SocketRwProcessor;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   服务器对象
 */

public final class NioServer {

    private ServerLock mServerLock;
    private SocketAcceptProcessor               mSocketAcceptProcessor;
    private SocketRwProcessor mSocketWRProcessor;
    private ConcurrentLinkedQueue<NioClient>    mClientQueen  = new ConcurrentLinkedQueue<>();

    public NioServer(ServerConfig mServerInfo, BaseMessageProcessor mMessageProcessor) throws IOException {
        this.mServerLock = new ServerLock();
        this.mSocketAcceptProcessor = new SocketAcceptProcessor(mServerInfo,mServerLock,mClientQueen,mMessageProcessor);
        this.mSocketWRProcessor     = new SocketRwProcessor(mClientQueen,mMessageProcessor);
    }

    public void start(){

        Thread mAcceptProcessorThread   = new Thread(this.mSocketAcceptProcessor);
        Thread mRwProcessorThread       = new Thread(this.mSocketWRProcessor);

        mAcceptProcessorThread.start();
        mRwProcessorThread.start();

        mServerLock.waitEnding();
    }
}
