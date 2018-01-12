package com.open.net.server.impl.udp.nio;

import com.open.net.server.message.Message;
import com.open.net.server.object.AbstractServerClient;
import com.open.net.server.object.AbstractServerMessageProcessor;
import com.open.net.server.object.ServerLog;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * author       :   long
 * created on   :   2017/12/6
 * description  :   客户端对象
 */

public class UdpNioClient extends AbstractServerClient {

	public static String TAG = "UdpNioClient";
	
    private DatagramChannel mSocketChannel;
    private static ByteBuffer mWriteByteBuffer = ByteBuffer.allocate(AbstractServerClient.PACKET_MAX_LENGTH_UDP);

    public void init(String mHost, int mPort , AbstractServerMessageProcessor messageProcessor, DatagramChannel socketChannel){
        super.init(mHost,mPort,messageProcessor);

        this.mSocketChannel = socketChannel;
    }

    @Override
    public boolean onRead() {
        return false;
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
                        mSocketChannel.send(mWriteByteBuffer,new InetSocketAddress(mHost,mPort));
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
                    mSocketChannel.send(mWriteByteBuffer,new InetSocketAddress(mHost,mPort));
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
