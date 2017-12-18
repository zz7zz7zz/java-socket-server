package com.open.net.server.structures;

import com.open.net.server.utils.CfgParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.HashMap;

/**
  # name  : server name 服务器名字
  # msgId    : server idsss
  # host  : host 主机ip
  # port  : port 端口
  # mc : max conect count 最大的连接数
  # bl : connect_backlog 客户连接请求队列的长度
         管理客户连接请求的任务是由操作系统来完成的。
         操作系统把这些连接请求存储在一个先进先出的队列中。
         许多操作系统限定了队列的最大长度，一般为50。
         当队列中的连接请求达到了队列的最大容量时，服务器进程所在的主机会拒绝新的连接请求。
         只有当服务器进程通过ServerSocket的accept()方法从队列中取出连接请求，使队列腾出空位时，队列才能继续加入新的连接请求。


 * author       :   long
 * created on   :   2017/11/30
 * description  :   服务器配置
 *
 */

public class ServerConfig {

    public String name = "";
    public int    id   = -1;
    public String host = "";
    public int    port = -1 ;

    //缓存配置 连接配置
    public int connect_max_count = 1000;
    public int connect_backlog = 50;

    //缓存配置
    public int  pool_client_size;

    public int pool_capacity_small;
    public int pool_capacity_middle;
    public int pool_capacity_large;

    public int pool_size_small;
    public int pool_size_middle;
    public int pool_size_large;

    public int pool_max_size_temporary_cache;

    //解析命令行参数
    public static void initCmdConfig(ServerConfig mServerConfig ,String[] args) {
        Options options = new Options();

        Option mOption = new Option("n","name",true,"set the server name");
        mOption.setRequired(true);
        options.addOption(mOption);

        mOption = new Option("i","id",true,"set the server id");
        mOption.setRequired(true);
        options.addOption(mOption);

        mOption = new Option("h","host",true,"set the server host");
        mOption.setRequired(false);
        options.addOption(mOption);

        mOption =new Option("p","port",true,"set the server port");
        mOption.setRequired(true);
        options.addOption(mOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options,args);
        } catch (ParseException e) {
            e.printStackTrace();
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "[Option] [VALUE] | ex : -n 'name' -i 9999 -h '192.168.0.1' -p 9999", options);
            System.exit(0);
        }

        mServerConfig.name = cmd.getOptionValue("n");
        mServerConfig.id   = Integer.valueOf(cmd.getOptionValue("i"));
        mServerConfig.host = cmd.getOptionValue("h");
        mServerConfig.port = Integer.valueOf(cmd.getOptionValue("p"));
    }

    //解析文件配置参数
    public static void initFileConfig(ServerConfig mServerConfig, String config_path) {
        HashMap<String,Object> ret = CfgParser.parseToMap(config_path);
        if(null !=ret){
            initFileConfig(mServerConfig,ret);
        }
    }


    private static void initFileConfig(ServerConfig mServerConfig, HashMap<String,Object> map){

        mServerConfig.name = CfgParser.getString(map,"server","name");

        mServerConfig.connect_max_count = CfgParser.getInt(map,"connect","connect_max_count");
        mServerConfig.connect_backlog = CfgParser.getInt(map,"connect","connect_backlog");

        mServerConfig.pool_client_size = CfgParser.getInt(map,"pool","pool_client_size");

        mServerConfig.pool_capacity_small = CfgParser.getInt(map,"pool","pool_capacity_small");
        mServerConfig.pool_capacity_middle = CfgParser.getInt(map,"pool","pool_capacity_middle");
        mServerConfig.pool_capacity_large = CfgParser.getInt(map,"pool","pool_capacity_large");

        mServerConfig.pool_size_small = CfgParser.getInt(map,"pool","pool_size_small");
        mServerConfig.pool_size_middle = CfgParser.getInt(map,"pool","pool_size_middle");
        mServerConfig.pool_size_large = CfgParser.getInt(map,"pool","pool_size_large");

        mServerConfig.pool_max_size_temporary_cache = CfgParser.getInt(map,"pool","pool_max_size_temporary_cache");

    }
}