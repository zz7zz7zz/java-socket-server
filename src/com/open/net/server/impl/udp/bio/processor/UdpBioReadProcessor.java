package com.open.net.server.impl.udp.bio.processor;

import com.open.net.server.GServer;
import com.open.net.server.impl.udp.bio.UdpBioClient;
import com.open.net.server.structures.BaseClient;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLock;
import com.open.net.server.structures.ServerLog;
import com.open.net.server.structures.pools.ClientsPool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :  客户连接处理类
 */

public class UdpBioReadProcessor implements Runnable
{
	public static String TAG = "UdpBioReadProcessor";
	
    public ServerConfig mServerInfo;
    public ServerLock mServerLock;
    public BaseMessageProcessor mMessageProcessor;

    public byte[] mWriteBuff  = new byte[65507];
    public byte[] mReadBuff  = new byte[65507];

    public UdpBioReadProcessor(ServerConfig mServerInfo, ServerLock mLock, BaseMessageProcessor mMessageProcessor) {
        this.mServerInfo = mServerInfo;
        this.mServerLock = mLock;
        this.mMessageProcessor = mMessageProcessor;
    }

    public void run() {

        try {
            DatagramSocket mSocket = new DatagramSocket(mServerInfo.port);
            DatagramPacket mReadDatagramPacket = new DatagramPacket(mReadBuff, mReadBuff.length);//创建发送方的数据报信息
            DatagramPacket mWriteDatagramPacket  = new DatagramPacket(mWriteBuff, mWriteBuff.length);//创建发送方的数据报信息

            while (true){
                mSocket.receive(mReadDatagramPacket);

                String mHost = mReadDatagramPacket.getAddress().getHostAddress();
                int    mPort = mReadDatagramPacket.getPort();

                UdpBioClient mClient = null;
                BaseClient client = GServer.getClient(mHost,mPort);
                if(null == client){
                    mClient = (UdpBioClient) ClientsPool.get();
                    if(null != mClient){
                        mClient.init(mHost,mPort,mMessageProcessor,mSocket,mWriteDatagramPacket,mReadDatagramPacket);
                    }else{
                    		ServerLog.getIns().log(TAG, "accept client success but ClientsPool.get() null Host "+ mHost + " port " + mPort );
                    }
                }else{
                    mClient = (UdpBioClient)client;
                }

                if(null!= mMessageProcessor) {
                    mMessageProcessor.onReceiveData(mClient, mReadDatagramPacket.getData(),mReadDatagramPacket.getOffset(),mReadDatagramPacket.getLength());
                    mMessageProcessor.onReceiveMessages(mClient);
                }
                mReadDatagramPacket.setLength(mReadDatagramPacket.getData().length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServerLog.getIns().log(TAG, "server exit");
        mServerLock.notifytEnding();

    }
}
