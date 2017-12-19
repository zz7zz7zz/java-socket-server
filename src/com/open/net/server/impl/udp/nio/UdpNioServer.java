package com.open.net.server.impl.udp.nio;

import com.open.net.server.impl.udp.nio.processor.UdpNioReadWriteProcessor;
import com.open.net.server.structures.AbstractMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;

import java.io.IOException;

/**
 * author       :   long
 * created on   :   2017/12/6
 * description  :   服务器对象
 */

public class UdpNioServer {

    private ServerLock mServerLock;
    private UdpNioReadWriteProcessor mUdpNioReadWriteProcessor;

    public UdpNioServer(ServerConfig mServerInfo , AbstractMessageProcessor mMessageProcessor) throws IOException {
        this.mServerLock = new ServerLock();
        this.mUdpNioReadWriteProcessor     = new UdpNioReadWriteProcessor(mServerInfo,mMessageProcessor);
    }

    public void start(){

        Thread mUdpNioReadWriteProcessor       = new Thread(this.mUdpNioReadWriteProcessor);
        mUdpNioReadWriteProcessor.start();

        mServerLock.waitEnding();
    }

}
