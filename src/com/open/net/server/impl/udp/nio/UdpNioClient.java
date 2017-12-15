package com.open.net.server.impl.udp.nio;

import com.open.net.server.structures.BaseClient;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.message.Message;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * author       :   long
 * created on   :   2017/12/6
 * description  :   客户端对象
 */

public class UdpNioClient extends BaseClient {

    private DatagramChannel mSocketChannel;
    private ByteBuffer mWriteByteBuffer = ByteBuffer.allocate(256*1024);

    public void init(String mHost, int mPort , BaseMessageProcessor messageProcessor, DatagramChannel socketChannel){
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
                    int writtenTotalLength;

                    while(true){

                        int putLength = leftLength > mWriteByteBuffer.capacity() ? mWriteByteBuffer.capacity() : leftLength;
                        mWriteByteBuffer.put(msg.data,offset,putLength);
                        mWriteByteBuffer.flip();
                        offset      += putLength;
                        leftLength  -= putLength;

                        int writtenLength   = mSocketChannel.write(mWriteByteBuffer);//客户端关闭连接后，此处将抛出异常
                        writtenTotalLength  = writtenLength;

                        while(writtenLength > 0 && mWriteByteBuffer.hasRemaining()){
                            writtenLength       = mSocketChannel.write(mWriteByteBuffer);
                            writtenTotalLength += writtenLength;
                        }
                        mWriteByteBuffer.clear();

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
            mMessageId = pollWriteMessageId();
            while (null != mMessageId) {
                mMessageProcessor.mWriteMessageQueen.remove(this, mMessageId);
                mMessageId = pollWriteMessageId();
            }
        }

        return writeRet;
    }
}
