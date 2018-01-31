package com.open.net.server.object;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
# name  : server name 服务器名字
# id    : server id   服务器id
# host  : host 主机ip
# port  : port 端口

* author       :   long
* created on   :   2017/11/30
* description  :   服务器配置
*
*/

public class ArgsConfig {

    //基本信息
	public int    server_type = -1;
    public String name = "";
    public short    id   = -1;
    public String host = "";
    public int    port = -1 ;
    
    //-------------------------------------------------------------------------------------------
    
    //解析命令行参数
    public final void initArgsConfig(String[] args) {
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
            formatter.printHelp( "[Option] [VALUE] | ex : -n 'Access' -i 1 -h '192.168.0.1' -p 9999", options);
            System.exit(0);
        }

        initArgsConfig(cmd);
    }
    
    protected void initArgsConfig(CommandLine cmd){
        name = cmd.getOptionValue("n");
        id   = Short.valueOf(cmd.getOptionValue("i"));
        if(cmd.hasOption("h")){
        	host = cmd.getOptionValue("h");
        }
        port = Integer.valueOf(cmd.getOptionValue("p"));
    }

	@Override
	public String toString() {
		return "ArgsConfig [name=" + name + ", id=" + id + ", host=" + host + ", port=" + port + "]";
	}

}
