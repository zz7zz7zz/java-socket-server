package com.open.net.server.impl.udp.nio;

import com.open.net.server.impl.udp.nio.processor.UdpNioReadWriteProcessor;
import com.open.net.server.structures.AbstractMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;

import java.io.IOException;

/**
 * author       :   Administrator
 * created on   :   2017/12/6
 * description  :
 */

public class UdpNioServer {

    private ServerLock mServerLock;
    private UdpNioReadWriteProcessor mSocketWRProcessor;

    public UdpNioServer(ServerConfig mServerInfo , AbstractMessageProcessor mMessageProcessor) throws IOException {
        this.mServerLock = new ServerLock();
        this.mSocketWRProcessor     = new UdpNioReadWriteProcessor(mServerInfo,mMessageProcessor);
    }

    public void start(){

        Thread mRwProcessorThread       = new Thread(this.mSocketWRProcessor);
        mRwProcessorThread.start();

        mServerLock.waitEnding();
    }

}
