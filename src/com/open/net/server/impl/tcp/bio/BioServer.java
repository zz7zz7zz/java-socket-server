package com.open.net.server.impl.tcp.bio;

import com.open.net.server.impl.tcp.bio.processor.BioAcceptProcessor;
import com.open.net.server.impl.tcp.bio.processor.BioReadWriteProcessor;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerConfig;
import com.open.net.server.object.ServerLock;
import com.open.net.server.object.ServerLog;
import com.open.net.server.object.ServerLog.LogListener;

import java.io.IOException;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   服务器对象
 */

public class BioServer {

    private ServerLock mServerLock;
    private BioAcceptProcessor mBioAcceptProcessor;
    private BioReadWriteProcessor mBioReadWriteProcessor;

    public BioServer(ServerConfig mServerInfo, AbstractServerMessageProcessor mMessageProcessor,LogListener mLogListener) throws IOException {
        this.mServerLock = new ServerLock();
        this.mBioAcceptProcessor = new BioAcceptProcessor(mServerInfo,mServerLock,mMessageProcessor);
        this.mBioReadWriteProcessor = new BioReadWriteProcessor(mMessageProcessor);
        ServerLog.getIns().setLogListener(mLogListener);
    }

    public void start(){

        Thread mBioAcceptProcessorThread   = new Thread(this.mBioAcceptProcessor);
        Thread mBioReadWriteProcessorThread       = new Thread(this.mBioReadWriteProcessor);

        mBioAcceptProcessorThread.start();
        mBioReadWriteProcessorThread.start();

        mServerLock.waitEnding();
    }

}
