package com.open.net.server.impl.tcp.bio.processor;

import com.open.net.server.impl.tcp.bio.BioClient;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.pools.ClientsPool;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;
import com.open.net.server.structures.ServerLog;
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

public class BioAcceptProcessor implements Runnable
{
	public static String TAG = "BioAcceptProcessor";
	
    public ServerConfig mServerInfo;
    public ServerLock mServerLock;
    public BaseMessageProcessor mMessageProcessor;

    public BioAcceptProcessor(ServerConfig mServerInfo, ServerLock mLock, BaseMessageProcessor mMessageProcessor) {
        this.mServerInfo = mServerInfo;
        this.mServerLock = mLock;
        this.mMessageProcessor = mMessageProcessor;
    }

    public void run() {
        ServerSocket mServerSocket = null;
        try {
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

            while (true){
                Socket mSocketClient = mServerSocket.accept();
                if(null != mSocketClient){
                    String mHost = mSocketClient.getInetAddress().getHostAddress();
                    int    mPort = mSocketClient.getPort();
                    
                    BioClient mClient = (BioClient) ClientsPool.get();
                    if(null != mClient){
                        mClient.init(mHost,mPort,mSocketClient,mMessageProcessor);
                        
                        ServerLog.getIns().log(TAG, "accept client "+ mClient.mClientId +" Host "+ mHost + " port " + mPort );
                    }else{
                        ServerLog.getIns().log(TAG, "accept client null Host "+ mHost + " port " + mPort );
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        	if(null != mServerSocket){
        		try {
					mServerSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }

        ServerLog.getIns().log(TAG, "server exit");
        mServerLock.notifytEnding();

    }
}
