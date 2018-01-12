package com.open.net.server.impl.udp.nio;

import com.open.net.server.impl.udp.nio.processor.UdpNioReadWriteProcessor;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerConfig;
import com.open.net.server.object.ServerLock;
import com.open.net.server.object.ServerLog;
import com.open.net.server.object.ServerLog.LogListener;

import java.io.IOException;

/**
 * author       :   long
 * created on   :   2017/12/6
 * description  :   服务器对象
 */

public class UdpNioServer {

    private ServerLock mServerLock;
    private UdpNioReadWriteProcessor mUdpNioReadWriteProcessor;

    public UdpNioServer(ServerConfig mServerInfo , AbstractServerMessageProcessor mMessageProcessor,LogListener mLogListener) throws IOException {
        this.mServerLock = new ServerLock();
        this.mUdpNioReadWriteProcessor     = new UdpNioReadWriteProcessor(mServerInfo,mMessageProcessor);
        ServerLog.getIns().setLogListener(mLogListener);
    }

    public void start(){

        Thread mUdpNioReadWriteProcessor       = new Thread(this.mUdpNioReadWriteProcessor);
        mUdpNioReadWriteProcessor.start();

        mUdpNioReadWriteProcessor.setName("Udp-Nio-ReadWrite-Thread");
        
        mServerLock.waitEnding();
    }

}
