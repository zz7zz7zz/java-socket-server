package com.open.net.server.message;

import java.util.HashSet;
import java.util.Set;

import com.open.net.server.object.AbstractServerClient;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   消息
 */

public final class Message {

    private static long G_MESSAGE_ID = 0;
    public long msgId;

    public int src_reuse_type;
    public int dst_reuse_type;

    public byte[] data;//共享数组中的数据
    public int capacity;//数组中的容量
    public int block_index;//在块中的索引
    public int offset;//在数组的偏移量
    public int length;//有效长度

    //接受这个消息的客户端对象，一个消息有可能有多个接收对象，比如广播/多播，只有当消息对象为0的时候才清空和回收这个消息
    public Set<AbstractServerClient> mReceivers = new HashSet<AbstractServerClient>();

    public Message() {
        reset();
    }

    public void reset() {
        ++G_MESSAGE_ID;

        msgId = G_MESSAGE_ID;
        src_reuse_type = 0;
        dst_reuse_type = 0;

        data = null;
        capacity = 0;
        block_index = 0;
        offset = 0;
        length = 0;

        mReceivers.clear();
    }

}
