package com.open.net.server.impl.udp.nio.processor;

import com.open.net.server.GServer;
import com.open.net.server.impl.udp.nio.UdpNioClient;
import com.open.net.server.structures.BaseClient;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLog;
import com.open.net.server.structures.pools.ClientsPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * author       :   long
 * created on   :   2017/12/6
 * description  :   数据读写处理类
 */

public class UdpNioReadWriteProcessor implements Runnable{

	public static String TAG = "UdpNioReadWriteProcessor";
	
    public ServerConfig mServerInfo;
    public BaseMessageProcessor mMessageProcessor;

    private DatagramChannel mDatagramChannel;
    private Selector mSelector = null;
    private ByteBuffer mReadByteBuffer  = ByteBuffer.allocate(65507);


    public UdpNioReadWriteProcessor(ServerConfig mServerInfo, BaseMessageProcessor mMessageProcessor) {
        this.mServerInfo = mServerInfo;
        this.mMessageProcessor = mMessageProcessor;
    }

    @Override
    public void run() {

        try {
            mSelector = Selector.open();
            mDatagramChannel = DatagramChannel.open();
            mDatagramChannel.configureBlocking(false);
            mDatagramChannel.socket().bind(new InetSocketAddress(mServerInfo.port));
            mDatagramChannel.register(mSelector, SelectionKey.OP_READ);

            //开始读写
            boolean isExit = false;
            while(!isExit) {

                int readKeys = mSelector.select();
                if(readKeys > 0){
                    Iterator<SelectionKey> selectedKeys = mSelector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key =  selectedKeys.next();
                        selectedKeys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isReadable()) {

                                DatagramChannel mDatagramChannel =  (DatagramChannel)key.channel();
                                SocketAddress clientAddress =mDatagramChannel.receive(mReadByteBuffer);
                                String [] clientInfo = clientAddress.toString().replace("/", "").split(":");
                                String mHost = clientInfo[0];
                                int mPort = Integer.valueOf(clientInfo[1]);

                                UdpNioClient mClient;
                                BaseClient client = GServer.getClient(mHost,mPort);
                                if(null == client){
                                    mClient = (UdpNioClient) ClientsPool.get();
                                    if(null != mClient){
                                        mClient.init(mHost,mPort,mMessageProcessor,mDatagramChannel);
                                        ServerLog.getIns().log(TAG, "accept client "+ mClient.mClientId +" Host "+ mHost + " port " + mPort );
                                    }else{
                                    	   ServerLog.getIns().log(TAG, "accept client null Host "+ mHost + " port " + mPort );
                                    }
                                }else{
                                    mClient = (UdpNioClient)client;
                                }

                                mReadByteBuffer.flip();
                                if(mReadByteBuffer.remaining() > 0){
                                    mMessageProcessor.onReceiveData(mClient, mReadByteBuffer.array(), 0 , mReadByteBuffer.remaining());
                                    mMessageProcessor.onReceiveMessages(mClient);
                                }
                                mReadByteBuffer.clear();

                        }else if (key.isWritable()) {

                            for (BaseClient mClient: mMessageProcessor.mWriteMessageQueen.mWriteClientSet) {
                                if(null != mClient){
                                    mClient.onWrite();
                                }
                            }
                            mMessageProcessor.mWriteMessageQueen.mWriteClientSet.clear();
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    }
                }

                if(isExit ){
                    break;
                }

                if(mMessageProcessor.mWriteMessageQueen.mWriteClientSet.size() > 0) {
                    SelectionKey key= mDatagramChannel.keyFor(mSelector);
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
