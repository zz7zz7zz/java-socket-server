package com.open.net.server.impl.udp.bio;

import com.open.net.server.impl.udp.bio.processor.SocketReadProcessor;
import com.open.net.server.impl.udp.bio.processor.SocketWriteProcessor;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;

import java.io.IOException;

/**
 * author       :   Administrator
 * created on   :   2017/12/6
 * description  :
 */

public class UdpBioServer {

    private ServerLock mServerLock;
    private SocketReadProcessor mSocketReadProcessor;
    private SocketWriteProcessor mSocketWRProcessor;

    public UdpBioServer(ServerConfig mServerInfo ,BaseMessageProcessor mMessageProcessor) throws IOException {
        this.mServerLock = new ServerLock();
        this.mSocketReadProcessor   = new SocketReadProcessor(mServerInfo,mServerLock,mMessageProcessor);
        this.mSocketWRProcessor     = new SocketWriteProcessor(mMessageProcessor);
    }

    public void start(){

        Thread mRwProcessorThread       = new Thread(this.mSocketWRProcessor);
        Thread mRProcessorThread       = new Thread(this.mSocketReadProcessor);

        mRwProcessorThread.start();
        mRProcessorThread.start();

        mServerLock.waitEnding();
    }

}
