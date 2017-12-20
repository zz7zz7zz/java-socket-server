package com.open.net.main;

import com.open.net.server.GServer;
import com.open.net.server.impl.udp.nio.UdpNioClient;
import com.open.net.server.impl.udp.nio.UdpNioServer;
import com.open.net.server.structures.AbstractClient;
import com.open.net.server.structures.AbstractMessageProcessor;
import com.open.net.server.structures.ServerConfig;
import com.open.net.server.structures.ServerLog;
import com.open.net.server.structures.ServerLog.LogListener;
import com.open.net.server.structures.message.Message;
import com.open.util.log.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :  服务器入口
 */

public class MainUdpNioServer {

    public static void main_start(String [] args){

    	//1.配置初始化
        ServerConfig mServerInfo = new ServerConfig();
        mServerInfo.initArgsConfig(args);
        mServerInfo.initFileConfig("./conf/server.config");
        
        //2.数据初始化
        GServer.init(mServerInfo, UdpNioClient.class);
        
        //3.日志初始化
        Logger.init("./conf/log.config");
        Logger.addFilterTraceElement(ServerLog.class.getName());
        Logger.addFilterTraceElement(mLogListener.getClass().getName());
        ServerLog.getIns().setLogListener(mLogListener);
        Logger.v("-------Server------"+ mServerInfo.toString());
        
        //4.连接初始化
        Logger.v("-------Server------start---------");
        try {
            UdpNioServer mBioServer = new UdpNioServer(mServerInfo,mMessageProcessor);
            mBioServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.v("-------Server------end---------");
    }

    //-------------------------------------------------------------------------------------------
    public static AbstractMessageProcessor mMessageProcessor = new AbstractMessageProcessor() {

        private ByteBuffer mWriteBuffer  = ByteBuffer.allocate(128*1024);

        protected void onReceiveMessage(AbstractClient client, Message msg){
        	
            Logger.v("--onReceiveMessage()- rece  "+new String(msg.data,msg.offset,msg.length));
            String data ="MainUdpNioServer--onReceiveMessage()--src_reuse_type "+msg.src_reuse_type
                    + " dst_reuse_type " + msg.dst_reuse_type
                    + " block_index " +msg.block_index
                    + " offset " +msg.offset;
            Logger.v("--onReceiveMessage()--reply "+data);
            
            byte[] response = data.getBytes();

            mWriteBuffer.clear();
            mWriteBuffer.put(response,0,response.length);
            mWriteBuffer.flip();
//        unicast(client,mWriteBuffer.array(),0,response.length);
            broadcast(mWriteBuffer.array(),0,response.length);
            mWriteBuffer.clear();
        }
    };

    public static LogListener mLogListener = new LogListener(){

		@Override
		public void onLog(String tag, String msg) {
			Logger.v(msg);
		}
    };
    
}
