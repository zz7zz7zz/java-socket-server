package com.open.net.server.impl.udp.bio;

import com.open.net.server.impl.udp.bio.processor.UdpBioReadProcessor;
import com.open.net.server.impl.udp.bio.processor.UdpBioWriteProcessor;
import com.open.net.server.object.AbstractMessageProcessor;
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

public class UdpBioServer {

    private ServerLock mServerLock;
    private UdpBioReadProcessor mUdpBioReadProcessor;
    private UdpBioWriteProcessor mUdpBioWriteProcessor;

    public UdpBioServer(ServerConfig mServerInfo ,AbstractMessageProcessor mMessageProcessor,LogListener mLogListener) throws IOException {
        this.mServerLock = new ServerLock();
        this.mUdpBioReadProcessor   = new UdpBioReadProcessor(mServerInfo,mServerLock,mMessageProcessor);
        this.mUdpBioWriteProcessor     = new UdpBioWriteProcessor(mMessageProcessor);
        ServerLog.getIns().setLogListener(mLogListener);
    }

    public void start(){

        Thread mUdpBioWriteProcessorThread       = new Thread(this.mUdpBioWriteProcessor);
        Thread mUdpBioReadProcessorThread       = new Thread(this.mUdpBioReadProcessor);

        mUdpBioWriteProcessorThread.start();
        mUdpBioReadProcessorThread.start();

        mServerLock.waitEnding();
    }

}
