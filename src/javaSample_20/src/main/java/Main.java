import com.fasterxml.jackson.databind.ObjectMapper;
import conn.Channel;
import conn.GeneralChannel;
import json.Message;
import json.config.Config;

import java.io.File;
import java.util.Date;
import java.util.List;


public class Main {

    public static Config config;

    public static Dijkstra graph;

    public  static int high_way_id_tree=-1;

    public  static int high_way_id_add=-1;

    public static boolean addition_one=false;

    public static double curTime() {
        return new Date().getTime() / 1000.0;
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        config = objectMapper.readValue(new File("/home/config/client.json"), Config.class);
//        config = objectMapper.readValue(new File("resources/client.json"), Config.class);

        /**************************************************************构建基础图*******************************************************************/
//        高速通道总数
        int high_count=config.channelConfig.highSpeed.maxCount;

//        构建基础图
        graph=new Dijkstra(config.mainConfig.nodeCount+1);
        for (int i=2;i<=Main.config.mainConfig.nodeCount;i++){
            if(high_count>0){
                Util.addEdge_NoWeight(graph,i,i/2);
                high_count--;
                Main.high_way_id_tree=i;
            }else{
                Util.addEdge_NoWeight(graph,i,i/2);
            }
            if((i*2+1)>config.mainConfig.nodeCount&&(i%2)==1
                    &&(i+1)<=config.mainConfig.nodeCount){
                if(high_count>0){
                    Util.addEdge_NoWeight(graph,i,i+1);
                    high_count--;
                    Main.high_way_id_add=i;
                }else{
                    Util.addEdge_NoWeight(graph,i,i+1);
                }
            }
        }
        if(high_count>0) {
            Util.addEdge_NoWeight(graph, 1, config.mainConfig.nodeCount);
            Main.addition_one=true;
        }else{
            Util.addEdge_NoWeight(graph, 1, config.mainConfig.nodeCount);
        }
        /**************************************************************构建基础图*******************************************************************/

        Channel channel = new GeneralChannel();
        channel.initConfig(config);
        mainloop(channel);
    }

    public static void mainloop(Channel channel){
        Scheduler scheduler = new Scheduler(channel);

        /**************************************************************构建基础图*******************************************************************/
        if(Main.config.index==1){
            if(Main.addition_one) {
                scheduler.sendChannelBuild(Main.config.mainConfig.nodeCount, Const.STATE_REQUEST, Const.ERR_CODE_NONE, Const.CHANNEL_TYPE_FAST);
            }else{
                scheduler.sendChannelBuild(Main.config.mainConfig.nodeCount, Const.STATE_REQUEST, Const.ERR_CODE_NONE, Const.CHANNEL_TYPE_NORMAL);
            }
        }
        if(Main.config.index>=2){
            if(Main.config.index<=Main.high_way_id_tree){
                scheduler.sendChannelBuild(Main.config.index/2,Const.STATE_REQUEST,Const.ERR_CODE_NONE,Const.CHANNEL_TYPE_FAST);
            }else{
                scheduler.sendChannelBuild(Main.config.index/2,Const.STATE_REQUEST,Const.ERR_CODE_NONE,Const.CHANNEL_TYPE_NORMAL);
            }

            if((Main.config.index*2+1)>Main.config.mainConfig.nodeCount
                    &&(Main.config.index%2)==1&&(Main.config.index+1)<=Main.config.mainConfig.nodeCount){
                if(Main.config.index<=Main.high_way_id_add){
                    scheduler.sendChannelBuild(Main.config.index+1,Const.STATE_REQUEST,Const.ERR_CODE_NONE,Const.CHANNEL_TYPE_FAST);
                }else{
                    scheduler.sendChannelBuild(Main.config.index+1,Const.STATE_REQUEST,Const.ERR_CODE_NONE,Const.CHANNEL_TYPE_NORMAL);
                }
            }
        }
        /*****************************************************************构建基础图形*************************************************************/
        while (true) {
            try {
                List<Message> message = channel.recv();
                for (Message msg : message) {
                    scheduler.onRecv(msg);
                }
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
