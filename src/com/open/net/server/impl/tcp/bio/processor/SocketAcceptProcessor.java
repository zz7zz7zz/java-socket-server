package com.open.net.server.impl.tcp.bio.processor;

import com.open.net.server.impl.tcp.bio.BioClient;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.pools.ClientsPool;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;
import com.open.net.server.utils.TextUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :  客户连接处理类
 */

public class SocketAcceptProcessor implements Runnable
{
    public ServerConfig mServerInfo;
    public ServerLock mServerLock;
    public BaseMessageProcessor mMessageProcessor;

    public SocketAcceptProcessor(ServerConfig mServerInfo, ServerLock mLock, BaseMessageProcessor mMessageProcessor) {
        this.mServerInfo = mServerInfo;
        this.mServerLock = mLock;
        this.mMessageProcessor = mMessageProcessor;
    }

    public void run() {

        try {
            ServerSocket mServerSocket;
            if(mServerInfo.port > 0 ){
                if(mServerInfo.connect_backlog > 0 ){
                    if(!TextUtils.isEmpty(mServerInfo.host)){
                        mServerSocket = new ServerSocket(mServerInfo.port,mServerInfo.connect_backlog, InetAddress.getByName(mServerInfo.host));
                    }else{
                        mServerSocket = new ServerSocket(mServerInfo.port,mServerInfo.connect_backlog);
                    }
                }else {
                    mServerSocket = new ServerSocket(mServerInfo.port);
                }
            }else{
                mServerSocket = new ServerSocket();
            }

            System.out.println("-------BioServerSocketRunnable-----A----"+ mServerSocket.getInetAddress() + " port " + mServerSocket.getLocalPort());

            while (true){
                Socket mSocketClient = mServerSocket.accept();
                if(null != mSocketClient){
                    BioClient mClient = (BioClient) ClientsPool.get();
                    if(null != mClient){
                        mClient.init(mSocketClient,mMessageProcessor);
                    }else{
                        System.out.println("-------BioServerSocketRunnable-----B----");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("-------BioServerSocketRunnable-----C----");

        mServerLock.notifytEnding();

    }
}
