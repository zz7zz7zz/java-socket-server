package com.open.net.server.impl.tcp.nio.processor;

import com.open.net.server.impl.tcp.nio.NioClient;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;
import com.open.net.server.structures.pools.ClientsPool;
import com.open.net.server.utils.TextUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :  客户连接处理类
 */

public final class SocketAcceptProcessor implements Runnable
{
    private ServerConfig mServerInfo;
    private ServerLock mServerLock;
    private ConcurrentLinkedQueue<NioClient> mClientQueen;
    public BaseMessageProcessor mMessageProcessor;

    public SocketAcceptProcessor(ServerConfig mServerInfo, ServerLock mServerLock, ConcurrentLinkedQueue<NioClient> mClientQueen,BaseMessageProcessor mMessageProcessor) {
        this.mServerInfo = mServerInfo;
        this.mServerLock = mServerLock;
        this.mClientQueen = mClientQueen;
        this.mMessageProcessor = mMessageProcessor;
    }

    public void run() {

        try {
            ServerSocketChannel mServerSocket = ServerSocketChannel.open();

            if(mServerInfo.port > 0 ){
                if(mServerInfo.connect_backlog > 0 ){
                    if(!TextUtils.isEmpty(mServerInfo.host)){
                        mServerSocket.bind(new InetSocketAddress(mServerInfo.host,mServerInfo.port),mServerInfo.connect_backlog);
                    }else{
                        mServerSocket.bind(new InetSocketAddress(mServerInfo.port),mServerInfo.connect_backlog);
                    }
                }else {
                    mServerSocket.bind(new InetSocketAddress(mServerInfo.port));
                }
            }else{

                while (true){
                    try {
                        int port = (int)(1024+Math.random()*(0xffff-1024+1));
                        mServerSocket.bind(new InetSocketAddress(port));
                        break;
                    }catch (Exception e){
                        e.printStackTrace();
                        continue;
                    }
                }
            }

            while (true){
                SocketChannel mSocketClient = mServerSocket.accept();
                if(null != mSocketClient){
                	
                    String [] clientInfo = mSocketClient.getRemoteAddress().toString().replace("/", "").split(":");
                    String mHost = clientInfo[0];
                    int mPort = Integer.valueOf(clientInfo[1]);
      
                    System.out.println("-------SocketAcceptProcessor-----A----"+ mHost + " port " + mPort);
                    
                    NioClient mClient = (NioClient) ClientsPool.get();
                    if(null != mClient){
                        mClient.init(mHost,mPort,mSocketClient,mMessageProcessor);
                        mClientQueen.add(mClient);
                    }else{
                        System.out.println("-------SocketAcceptProcessor-----B----");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("-------SocketAcceptProcessor-----C----");

        mServerLock.notifytEnding();
    }
}