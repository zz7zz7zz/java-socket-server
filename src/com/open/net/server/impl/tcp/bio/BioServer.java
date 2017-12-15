package com.open.net.server.impl.tcp.bio;

import com.open.net.server.impl.tcp.bio.processor.SocketAcceptProcessor;
import com.open.net.server.impl.tcp.bio.processor.SocketRwProcessor;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;

import java.io.IOException;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   服务器对象
 */

public class BioServer {

    private ServerLock mServerLock;
    private SocketAcceptProcessor mSocketAcceptProcessor;
    private SocketRwProcessor mSocketWrProcessor;

    public BioServer(ServerConfig mServerInfo, BaseMessageProcessor mMessageProcessor) throws IOException {
        this.mServerLock = new ServerLock();
        this.mSocketAcceptProcessor = new SocketAcceptProcessor(mServerInfo,mServerLock,mMessageProcessor);
        this.mSocketWrProcessor     = new SocketRwProcessor(mMessageProcessor);
    }

    public void start(){

        Thread mAcceptProcessorThread   = new Thread(this.mSocketAcceptProcessor);
        Thread mRwProcessorThread       = new Thread(this.mSocketWrProcessor);

        mAcceptProcessorThread.start();
        mRwProcessorThread.start();

        mServerLock.waitEnding();
    }

}
