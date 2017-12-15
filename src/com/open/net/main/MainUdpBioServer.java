package com.open.net.main;

import com.open.net.server.GServer;
import com.open.net.server.impl.udp.bio.UdpBioClient;
import com.open.net.server.impl.udp.bio.UdpBioServer;
import com.open.net.server.structures.BaseClient;
import com.open.net.server.structures.BaseMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.message.Message;
import com.open.util.log.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :  服务器入口
 */

public class MainUdpBioServer {

    public static void main_start(String [] args){

        ServerConfig mServerInfo = new ServerConfig();
        ServerConfig.initCmdConfig(mServerInfo,args);
        ServerConfig.initFileConfig(mServerInfo,"./conf/server.config");
        
        Logger.init("./conf/log.config");
        Logger.v(LogAutor.ADMIN, "MainBioServer", "-------work------start---------");

        GServer.init(mServerInfo, UdpBioClient.class);

        try {
            UdpBioServer mBioServer = new UdpBioServer(mServerInfo,new MeMessageProcessor());
            mBioServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.v(LogAutor.ADMIN, "MainUdpBioServer", "-------work------end---------");
    }

    public static class MeMessageProcessor extends BaseMessageProcessor {

        private ByteBuffer mWriteBuffer  = ByteBuffer.allocate(256*1024);

        public void onReceiveMessage(BaseClient client, Message msg){
        	
            Logger.v(LogAutor.ADMIN, "MainUdpBioServer", "--onReceiveMessage()--"+new String(msg.data,msg.offset,msg.length));

            String data ="MainUdpBioServer--onReceiveMessage()--src_reuse_type "+msg.src_reuse_type
                    + " dst_reuse_type " + msg.dst_reuse_type
                    + " block_index " +msg.block_index
                    + " offset " +msg.offset;
            byte[] response = data.getBytes();
            System.out.println(data);


            mWriteBuffer.clear();
            mWriteBuffer.put(response,0,response.length);
            mWriteBuffer.flip();
//        unicast(client,mWriteBuffer.array(),0,response.length);
            broadcast(mWriteBuffer.array(),0,response.length);
            mWriteBuffer.clear();
        }
    }

}
