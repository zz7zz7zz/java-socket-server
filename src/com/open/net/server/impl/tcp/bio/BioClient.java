package com.open.net.server.impl.tcp.bio;

import com.open.net.server.structures.BaseClient;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.message.Message;

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

public final class BioClient extends BaseClient {

    private Socket          mSocket =null;
    private OutputStream    mOutputStream =null;
    private InputStream     mInputStream =null;
    private Thread          mReadThread =null;
//    private WriteRunnable mWriter   = null;
//    private Thread mWriteThread      =null;

    public BioClient() {
        reset();
    }

    //--------------------------------------------------------------------------------------

    public void init(Socket socket,BaseMessageProcessor messageProcessor) throws IOException {
        super.init("",-1,messageProcessor);

        mSocket    = socket;
        mOutputStream = socket.getOutputStream();
        mInputStream = socket.getInputStream();
        mReadThread =new Thread(new ReadRunnable());
        mReadThread.start();
//        mWriter = new WriteRunnable();
//        mWriteThread =new Thread(mWriter);
//        mWriteThread.start();

        mHost = socket.getInetAddress().getHostAddress();
        mPort = socket.getPort();
    }

    public synchronized void onClose(){
        super.onClose();
        try {
                try {
                    if(null!= mSocket) {
                        mSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    mSocket =null;
                }

                try {
                    if(null!= mOutputStream) {
                        mOutputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    mOutputStream =null;
                }

                try {
                    if(null!= mInputStream) {
                        mInputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    mInputStream =null;
                }

//                try {
//                    if(null!= mWriteThread && mWriteThread.isAlive())
//                    {
//                        mWriteThread.interrupt();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }finally{
//                    mWriteThread =null;
//                }

                try {
                    if(null!= mReadThread && mReadThread.isAlive()) {
                        mReadThread.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    mReadThread =null;
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset(){
        super.reset();
        mSocket =null;
        mMessageProcessor = null;

        mOutputStream =null;
        mInputStream =null;
//        mWriter         =null;
//        mWriteThread     =null;
        mReadThread =null;
    }

    @Override
    public boolean onRead() {
        boolean readRet = false;
        try {
            int maximum_length = 8192;
            byte[] bodyBytes=new byte[maximum_length];
            int numRead;
            while((numRead= mInputStream.read(bodyBytes, 0, maximum_length))>0) {
                if(numRead > 0){
                    if(null!= mMessageProcessor)
                    {
                        mMessageProcessor.onReceiveData(BioClient.this,bodyBytes,0,numRead);
                        mMessageProcessor.onReceiveMessages(BioClient.this);
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

        mMessageProcessor.onReceiveMessages(this);

        //退出客户端的时候需要把要写给该客户端的数据清空
        if(!readRet){
            Long mMessageId = pollWriteMessageId();
            while (null != mMessageId) {
                mMessageProcessor.mWriteMessageQueen.remove(this, mMessageId);
                mMessageId = pollWriteMessageId();
            }
        }

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
            mMessageId = BioClient.this.pollWriteMessageId();
            while (null != mMessageId) {
                mMessageProcessor.mWriteMessageQueen.remove(BioClient.this, mMessageId);
                mMessageId = BioClient.this.pollWriteMessageId();
            }
        }
        return writeRet;
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
//            }finally {
//                System.out.println("client onClose when onWrite : " + BioClient.this.mClientId);
//                onClose();
//            }
//        }
//    }

    private class ReadRunnable implements Runnable
    {
        public void run() {

            onRead();

            System.out.println("client onClose when onRead : " + BioClient.this.mClientId);

            Long mMessageId = BioClient.this.pollWriteMessageId();
            while (null != mMessageId) {
                mMessageProcessor.mWriteMessageQueen.remove(BioClient.this, mMessageId);
                mMessageId = BioClient.this.pollWriteMessageId();
            }

            onClose();
        }
    }
}
