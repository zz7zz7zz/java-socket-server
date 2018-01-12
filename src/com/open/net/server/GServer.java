package com.open.net.server;

import com.open.net.server.message.MessageBuffer;
import com.open.net.server.object.AbstractServerClient;
import com.open.net.server.object.ServerConfig;
import com.open.net.server.object.ServerLog;
import com.open.net.server.pools.ClientsPool;
import com.open.net.server.pools.MessagePool;
import com.open.net.server.utils.KeyUtil;

import java.util.HashMap;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   服务器全局数据
 */

public final class GServer {
	
	public static String TAG = "GServer";
	
    //服务器信息
    public static ServerConfig mServerInfo;

    //已经连接的客户端信息
    private static HashMap<Long,AbstractServerClient> mClientsMap;
    private static HashMap<String,Long> mIpPortClientsMap;

    public static final void init(ServerConfig mServerInfo,Class<? extends AbstractServerClient> cls_client){

        GServer.mServerInfo = mServerInfo;

        mClientsMap         = new HashMap<>(mServerInfo.connect_max_count);
        mIpPortClientsMap   = new HashMap<>(mServerInfo.connect_max_count);

        AbstractServerClient.PACKET_MAX_LENGTH_TCP = mServerInfo.packet_max_length_tcp;
        AbstractServerClient.PACKET_MAX_LENGTH_UDP = mServerInfo.packet_max_length_udp;
        
        MessagePool.init(mServerInfo.pool_size_small + mServerInfo.pool_size_middle + mServerInfo.pool_size_large);
        MessageBuffer.init( mServerInfo.pool_capacity_small,mServerInfo.pool_capacity_middle,mServerInfo.pool_capacity_large,
                            mServerInfo.pool_size_small,mServerInfo.pool_size_middle,mServerInfo.pool_size_large,
                            mServerInfo.pool_max_size_temporary_cache);
        ClientsPool.init(mServerInfo.connect_max_count,cls_client);
    }

    //----------------------------------------------------------------------
    public static final void register(AbstractServerClient mClient){
        if(null != mClient){
            mClientsMap.put(mClient.mClientId,mClient);
            mIpPortClientsMap.put(KeyUtil.getKey(mClient.mHost,mClient.mPort),mClient.mClientId);
            
            ServerLog.getIns().log(TAG, "client "+ mClient.mClientId +" enter Host "+ mClient.mHost + " port " + mClient.mPort );
        }
    }

    public static final void unregister(AbstractServerClient mClient){
        if(null != mClient){
            ServerLog.getIns().log(TAG, "client "+ mClient.mClientId +" exit  Host "+ mClient.mHost + " port " + mClient.mPort );
            
            mClientsMap.remove(mClient.mClientId);
            mIpPortClientsMap.remove(KeyUtil.getKey(mClient.mHost,mClient.mPort));
            ClientsPool.put(mClient);
        }
    }

    public static final AbstractServerClient getClient(String mHost, int mPort){
        Long socketId = mIpPortClientsMap.get(KeyUtil.getKey(mHost,mPort));
        if(null != socketId){
            return mClientsMap.get(socketId);
        }
        return null;
    }

    public static boolean isExistClient(AbstractServerClient mClient){
        return mClient == mClientsMap.get(mClient.mClientId);
    }

    public static boolean isExistClient(String mHost, int mPort){
        Long socketId = mIpPortClientsMap.get(KeyUtil.getKey(mHost,mPort));
        if(null != socketId){
            return null != mClientsMap.get(socketId);
        }
        return false;
    }

    public static int getClientSize(){
        return mClientsMap.size();
    }

    public static HashMap<Long,AbstractServerClient> getClients(){
        return mClientsMap;
    }
}
