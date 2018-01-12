package com.open.net.server.impl.tcp.nio;

import com.open.net.server.message.Message;
import com.open.net.server.object.AbstractServerClient;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerLog;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   客户端对象
 */

public final class NioClient extends AbstractServerClient {

	public static String TAG = "NioClient";

	//因为Nio中所有的客户端注册Selector后，都是一个一个来读取，所以可以设置为全局属性
    private static ByteBuffer mReadByteBuffer  = ByteBuffer.allocate(PACKET_MAX_LENGTH_TCP);
    private static ByteBuffer mWriteByteBuffer = ByteBuffer.allocate(PACKET_MAX_LENGTH_TCP);
    
    public SocketChannel mSocketChannel = null;

    public NioClient() {
        reset();
    }
    //--------------------------------------------------------------------------------------

    public void init(String mHost, int mPort ,SocketChannel socketChannel,AbstractServerMessageProcessor mMessageProcessor){
        super.init(mHost,mPort,mMessageProcessor);
        this.mSocketChannel = socketChannel;
    }

    public void onClose() {        
        try {
        	if(null != mSocketChannel){
                mSocketChannel.socket().close();
                mSocketChannel.close();
                mSocketChannel = null;
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
		super.onClose();
    }

    public void reset(){
        super.reset();
        mSocketChannel = null;
    }

    @Override
    public boolean onRead() {
        boolean readRet = true;
        try{
            mReadByteBuffer.clear();
            int readReceiveLength = 0;
            while (true){
                int readLength = mSocketChannel.read(mReadByteBuffer);//客户端关闭连接后，此处将抛出异常/或者返回-1
                if(readLength == -1){
                    readRet = false;
                    break;
                }
                readReceiveLength += readLength;
                //如果一次性读满了，则先回调一次，然后接着读剩下的，目的是为了一次性读完单个通道的数据
                if(readReceiveLength == mReadByteBuffer.capacity()){
                    mReadByteBuffer.flip();
                    if(mReadByteBuffer.remaining() > 0){
                        this.mMessageProcessor.onReceiveData(this, mReadByteBuffer.array(), 0 , mReadByteBuffer.remaining());
                    }
                    mReadByteBuffer.clear();
                    readReceiveLength = 0;
                }

                if(readLength <= 0){
                    break;
                }
            }

            mReadByteBuffer.flip();
            if(mReadByteBuffer.remaining() > 0){
                this.mMessageProcessor.onReceiveData(this, mReadByteBuffer.array(), 0 , mReadByteBuffer.remaining());
            }
            mReadByteBuffer.clear();
            
        }catch (Exception e){
            e.printStackTrace();
            readRet = false;
        }

        mMessageProcessor.onReceiveDataCompleted(this);

        if(!readRet){
        	onSocketExit(2);
        }

        return readRet;
    }

    @Override
    public boolean onWrite() {
        boolean writeRet = true;
        Long mMessageId = pollWriteMessageId();
        try{
            //强制清空数据，防止channel.onWrite()时抛出异常而没有清空数据
            mWriteByteBuffer.clear();

            while (null != mMessageId){
                Message msg = mMessageProcessor.mWriteMessageQueen.mMessageMap.get(mMessageId);
                if(null == msg){
                    mMessageId = pollWriteMessageId();
                    continue;
                }

                //如果消息块的大小超过缓存的最大值，则需要分段写入后才丢弃消息，不能在数据未完全写完的情况下将消息丢弃;avoid BufferOverflowException
                if(mWriteByteBuffer.capacity() < msg.length){
                    int offset = 0;
                    int leftLength = msg.length;

                    while(true){
                        int putLength = leftLength > mWriteByteBuffer.capacity() ? mWriteByteBuffer.capacity() : leftLength;
                        mWriteByteBuffer.put(msg.data,offset,putLength);
                        mWriteByteBuffer.flip();
                        while(mWriteByteBuffer.hasRemaining()){
                            mSocketChannel.write(mWriteByteBuffer);//客户端关闭连接后，此处将抛出异常
                        }
                        mWriteByteBuffer.clear();
                        
                        offset      += putLength;
                        leftLength  -= putLength;
                        
                        if(leftLength <=0){
                            break;
                        }
                    }
                }else{
                    mWriteByteBuffer.put(msg.data,msg.offset,msg.length);
                    mWriteByteBuffer.flip();
                    while(mWriteByteBuffer.hasRemaining()){
                        mSocketChannel.write(mWriteByteBuffer);//客户端关闭连接后，此处将抛出异常
                    }
                    mWriteByteBuffer.clear();
                }

                mMessageProcessor.mWriteMessageQueen.remove(this,msg);
                mMessageId = pollWriteMessageId();
                mWriteByteBuffer.clear();
            }

        }catch (Exception e){
            e.printStackTrace();
            writeRet = false;
        }

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
