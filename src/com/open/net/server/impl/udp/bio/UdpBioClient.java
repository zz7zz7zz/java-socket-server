package com.open.net.server.impl.udp.bio;

import com.open.net.server.message.Message;
import com.open.net.server.object.AbstractServerClient;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerLog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * author       :   long
 * created on   :   2017/12/6
 * description  :   客户端对象
 */

public class UdpBioClient extends AbstractServerClient {

	public static String TAG = "UdpBioClient";
	
    private Thread          mReadThread =null;
    //-------------------------------------------------------------------------------------------
    private DatagramSocket mSocket;
    private DatagramPacket mWriteDatagramPacket ;
    private DatagramPacket mReadDatagramPacket ;

    public void init(String mHost, int mPort ,AbstractServerMessageProcessor messageProcessor, DatagramSocket mSocket, DatagramPacket mWriteDatagramPacket, DatagramPacket mReadDatagramPacket){
        super.init(mHost,mPort,messageProcessor);

        this.mSocket = mSocket;
        this.mWriteDatagramPacket = mWriteDatagramPacket;
        this.mReadDatagramPacket = mReadDatagramPacket;
    }

    @Override
    public synchronized void onClose() {
        try {
            if(null!= mReadThread && mReadThread.isAlive()) {
                mReadThread.interrupt();
                mReadThread =null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mWriteDatagramPacket = null;
        mReadDatagramPacket  = null;
        super.onClose();
    }

    @Override
    public boolean onRead() {
        return true;
    }

    @Override
    public boolean onWrite() {
        boolean writeRet = false;
        Long mMessageId = null;
        try{
            mMessageId  = pollWriteMessageId();
            while (null != mMessageId) {
                Message msg = mMessageProcessor.mWriteMessageQueen.mMessageMap.get(mMessageId);
                if (null == msg) {
                    mMessageId = pollWriteMessageId();
                    continue;
                }
                mWriteDatagramPacket.setAddress(InetAddress.getByName(mHost));
                mWriteDatagramPacket.setPort(mPort);
                mWriteDatagramPacket.setData(msg.data,msg.offset,msg.length);
                mSocket.send(mWriteDatagramPacket);

                mMessageProcessor.mWriteMessageQueen.remove(this,msg);
                mMessageId = pollWriteMessageId();
            }
            writeRet =  true;
        }catch (SocketException e){
            e.printStackTrace();
        }catch (IOException e1){
            e1.printStackTrace();
        }

        //退出客户端的时候需要把要写给该客户端的数据清空
        if(!writeRet){
            if(null != mMessageId){
                mMessageProcessor.mWriteMessageQueen.remove(this, mMessageId);
            }
            onSocketExit(1);
        }
        return writeRet;
    }
    
    public void onSocketExit(int exit_code){
    		ServerLog.getIns().log(TAG, String.format("client close id %d host %s port %d when %s", mClientId,mHost,mPort,(exit_code == 1 ? "write" : "read ")));
        onClose();
    }
}
