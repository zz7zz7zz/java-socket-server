package com.open.net.server.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public final class NetUtil {

	public final static void send_data_by_tcp_bio(String host,int port,byte[] data){
		
	}
	
	public final static void send_data_by_tcp_nio(String host,int port,byte[] data){
		
	}
	
	public final static void send_data_by_udp_bio(String host,int port,byte[] data){
		DatagramSocket socket = null;
		try {
			InetAddress address = InetAddress.getByName(host);
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			socket = new DatagramSocket();
			socket.send(packet);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(null != socket){
				socket.close();
			}
		}
	}
	
	public final static void send_data_by_udp_nio(String host,int port,byte[] data){
		
	}

}
