package com.open.net.server.impl.tcp.bio;

import com.open.net.server.message.Message;
import com.open.net.server.object.AbstractServerClient;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   客户端对象
 */

public final class BioClient extends AbstractServerClient {
	
	public static String TAG = "BioClient";

	private static int MAX_READ_LEN = 8192;
	
    private Socket          mSocket 	  = null;
    private OutputStream    mOutputStream = null;
    private InputStream     mInputStream  = null;
    private Thread          mReadThread   = null;
//    private WriteRunnable mWriter   = null;
//    private Thread mWriteThread      =null;

    public BioClient() {
        reset();
    }

    //--------------------------------------------------------------------------------------

    public void init(String mHost, int mPort ,Socket socket,AbstractServerMessageProcessor messageProcessor) throws IOException {
        super.init(mHost,mPort,messageProcessor);

        mSocket    		= socket;
        mOutputStream 	= socket.getOutputStream();
        mInputStream 	= socket.getInputStream();
        mReadThread 	= new Thread(new ReadRunnable());
        mReadThread.start();
//        mWriter = new WriteRunnable();
//        mWriteThread =new Thread(mWriter);
//        mWriteThread.start();
    }

    public synchronized void onClose(){

        //关闭输入出流
        try {
            if(null!= mSocket) {
                mSocket.close();
                mSocket =null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(null!= mOutputStream) {
                mOutputStream.close();
                mOutputStream =null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            if(null!= mInputStream) {
                mInputStream.close();
                mInputStream =null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            if(null!= mWriteThread && mWriteThread.isAlive()){
//                mWriteThread.interrupt();
//      		  mWriteThread =null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally{

//        }

        try {
            if(null!= mReadThread && mReadThread.isAlive()) {
                mReadThread.interrupt();
                mReadThread =null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        super.onClose();
    }

    public void reset(){
        super.reset();
        mSocket =null;
        mOutputStream =null;
        mInputStream =null;
        mReadThread =null;
//        mWriter         =null;
//        mWriteThread     =null;
    }

    @Override
    public boolean onRead() {
        boolean readRet = false;
        try {
            byte[] byteBuff=new byte[MAX_READ_LEN];
            int numRead;
            while((numRead= mInputStream.read(byteBuff, 0, MAX_READ_LEN))>0) {
                if(numRead > 0){
                    if(null!= mMessageProcessor){
                        mMessageProcessor.onReceiveData(BioClient.this,byteBuff,0,numRead);
                        mMessageProcessor.onReceiveDataCompleted(BioClient.this);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();//客户端主动socket.stopConnect()会调用这里 java.net.SocketException: Socket closed
            readRet = false;
        }catch (IOException e1) {
            e1.printStackTrace();
            readRet = false;
        }catch (Exception e2) {
            e2.printStackTrace();
            readRet = false;
        }

        if(null!= mMessageProcessor){
        	mMessageProcessor.onReceiveDataCompleted(this);
        }
        
        onSocketExit(2);
        return readRet;
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

                mOutputStream.write(msg.data,msg.offset,msg.length);
                mOutputStream.flush();

                mMessageProcessor.mWriteMessageQueen.remove(BioClient.this,msg);
                mMessageId = pollWriteMessageId();
            }
            writeRet =  true;
        }catch (SocketException e){
            e.printStackTrace();//发送的时候出现异常，说明socket被关闭了(服务器关闭)java.net.SocketException: sendto failed: EPIPE (Broken pipe)
        }catch (IOException e1){
            e1.printStackTrace();
        }

        //退出客户端的时候需要把要写给该客户端的数据清空
        if(!writeRet){
            if(null != mMessageId){
                mMessageProcessor.mWriteMessageQueen.remove(BioClient.this, mMessageId);
            }
            onSocketExit(1);
        }
        return writeRet;
    }

    public void onSocketExit(int exit_code){
    		ServerLog.getIns().log(TAG, String.format("client close id %d host %s port %d when %s", mClientId,mHost,mPort,(exit_code == 1 ? "write" : "read ")));
        onClose();
    }
    
//    @Override
//    public void addWriteMessageId(long id) {
//        super.addWriteMessageId(id);
//        if(null != mWriter){
//            mWriter.wakeup();
//        }
//    }

    //--------------------------------------------------------------------------------------
//    private class WriteRunnable implements Runnable
//    {
//
//        private final Object lock=new Object();
//
//        public void wakeup(){
//            synchronized (lock)
//            {
//                lock.notifyAll();
//            }
//        }
//
//        public void run() {
//            try {
//                while(true) {
//                    if(!onWrite()){
//                        break;
//                    }
//
//                    synchronized (lock) {
//                        lock.wait();
//                    }
//                }
//            }catch (Exception e) {
//                e.printStackTrace();
//            }
//            
//            onSocketExit(1);
//        }
//    }

    private class ReadRunnable implements Runnable
    {
        public void run() {
            onRead();
        }
    }
}
