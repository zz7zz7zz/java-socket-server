package com.open.net.server.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public final class NetUtil {

	public final static void send_data_by_tcp_bio(String host,int port,int timeout,byte[] data){
		Socket socket = null;
		OutputStream os = null;
		try {
			socket  =new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
			os = socket.getOutputStream();
			os.write(data);
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
			if(null !=os){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(null !=socket){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public final static void send_data_by_tcp_nio(String host,int port,int timeout,byte[] data){
		Selector mSelector     = null;
		SocketChannel mSocketChannel = null;

        try {
        	mSelector=Selector.open();
    		mSocketChannel = SocketChannel.open();
            mSocketChannel.configureBlocking(false);
			mSocketChannel.connect(new InetSocketAddress(host, port));
			mSocketChannel.register(mSelector, SelectionKey.OP_CONNECT);
			
			//连接
			boolean isConnectSuccess = false;
			int connect_timeout = timeout;
            int connectReady = 0;
            if(connect_timeout == -1){
                connectReady = mSelector.select();
            }else{
                connectReady = mSelector.select(connect_timeout);
            }
            if(connectReady > 0){
                Iterator<SelectionKey> selectedKeys = mSelector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                    	 boolean connectRet = true;
                         try{
                             boolean result;
                             SocketChannel socketChannel = (SocketChannel) key.channel();
                             result= socketChannel.finishConnect();//没有网络的时候也返回true;连不上的情况下会抛出java.net.ConnectException: Connection refused
                             if(result) {
                                 key.interestOps(SelectionKey.OP_READ);
                             }
                         }catch (Exception e){
                             e.printStackTrace();
                             connectRet = false;
                         }
                         
                        boolean ret = connectRet;
                        isConnectSuccess = ret;
                        if(!ret){
                            key.cancel();
                            key.attach(null);
                            key.channel().close();
                            break;
                        }
                    }
                }
            }else{
                isConnectSuccess = false;
                try{
                    Iterator<SelectionKey> selectedKeys = mSelector.keys().iterator();
                    while(selectedKeys.hasNext()){
                        SelectionKey key = selectedKeys.next();
                        key.cancel();
                        key.attach(null);
                        key.channel().close();
                    }
                }catch(Exception e2){
                    e2.printStackTrace();
                }
            }
            
            if(isConnectSuccess){
            	 ByteBuffer mWriteByteBuffer = ByteBuffer.wrap(data);
    			 while(mWriteByteBuffer.hasRemaining()){
                     mSocketChannel.write(mWriteByteBuffer);
                 }
            }
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
	        if(null!= mSocketChannel) {
	            try {
	                SelectionKey key = mSocketChannel.keyFor(mSelector);
	                if(null != key){
	                    key.cancel();
	                }
	                mSocketChannel.socket().close();
	                mSocketChannel.close();
	                mSocketChannel = null;
	            } catch (IOException e1) {
	                e1.printStackTrace();
	            }
	        }

	        if(null != mSelector){
	            try {
	                mSelector.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            mSelector = null;
	        }
		}
        
	}
	
	public final static void send_data_by_udp_bio(String host,int port,byte[] data){
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(host), port));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != socket){
				socket.close();
			}
		}
	}
	
	public final static void send_data_by_udp_nio(String host,int port,byte[] data){
		DatagramChannel  channel = null;
		try {
			channel = DatagramChannel.open();
		    channel.send(ByteBuffer.wrap(data), new InetSocketAddress(host,port));  
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != channel){
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
