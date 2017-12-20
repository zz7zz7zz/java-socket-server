package com.open.net.server.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public final class NetUtil {

	public final static void send_data_by_tcp_bio(String host,int port,byte[] data){
		
	}
	
	public final static void send_data_by_tcp_nio(String host,int port,byte[] data){
		
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
