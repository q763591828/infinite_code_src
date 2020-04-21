import conn.Channel;
import json.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scheduler {
    private Channel channel;
    Action[] action;
    Map<Integer, Integer> channelMap;

    public List<Integer> addition_node;

    public List<Integer> default_neighbor;


    public int init=0;

    public Scheduler(Channel channel) {
        this.channel = channel;
        int N = Main.config.mainConfig.nodeCount;

        action = new Action[N+1];
        for (int i=1; i<=N; i++) {
            action[i] = new Action(i, this);
        }
        channelMap = new HashMap<>();
        addition_node=new ArrayList<>();
        default_neighbor=new ArrayList<>();
        Util.init_default_list(default_neighbor);
    }

    public int getId() {
        return channel.getId();
    }

    public void onRecv(Message message) {

        switch (message.callType) {
//          预备消息，一般用于通道的构建
            case Const.CALL_TYPE_PREPARE:
                action[message.sysMessage.target].onPrepare(message);
                break;
//          消息的开始发送的点，消息终止节点
            case Const.CALL_TYPE_SEND:
                action[message.sysMessage.target].passMessageDoSend(message);
                break;
//          节点之间消息的传递
            case Const.CALL_TYPE_SYS:

                if(message.extMessage.get("notice")!=null){
//                  节点之间的notice
                    if(message.extMessage.get("notice").equals("2")) {
                        int start = Integer.parseInt(message.extMessage.get("start"));
                        int end = Integer.parseInt(message.extMessage.get("end"));
                        double delay = Double.parseDouble(message.extMessage.get("delay"));
                        Util.addEdge_NoWeight(Main.graph, start, end);
                        break;
                    }else if(message.extMessage.get("notice").equals("1")){
                        int start = Integer.parseInt(message.extMessage.get("start"));
                        int end = Integer.parseInt(message.extMessage.get("end"));
                        double delay = Double.parseDouble(message.extMessage.get("delay"));
                        Util.addEdge_NoWeight(Main.graph, start, end);
                        this.doNotice(start,end,delay,2);
                        break;
                    }
                }
//                直接最短路由算法
                if(message.extMessage.get("directed")!=null
                        &&message.extMessage.get("directed").equals("1")) {
                    int nextIndex = Integer.parseInt(message.extMessage.get("cur_index")) + 1;
                    String[] path = message.extMessage.get("path").split("_");
                    int nextClient = Integer.parseInt(path[nextIndex]);
                    action[nextClient].direct_passMessage(message);
//                    跳
                }else if(message.extMessage.get("jump")!=null
                        &&message.extMessage.get("jump").equals("1")){
                    action[message.sysMessage.target].jump(message);
                    break;
                }
                break;
//          通道建立相关
            case Const.CALL_TYPE_CHANNEL_BUILD:
//                通道建立成功
                if (message.channelId != 0) {
                    action[message.sysMessage.target].onSucc(message);
                    channelMap.put(message.channelId, message.sysMessage.target);
                } else {
                    switch (message.state) {
//                        收到集群通知，请求建立通道
                        case Const.STATE_NOTICE:
                            action[message.sysMessage.target].onRequest(message);
                            break;
//                        收到拒绝建立通道的请求，可能是集群的原因(总通道数，不够)，也可能是客户端的原因
                        case Const.STATE_REFUSE:
                            action[message.sysMessage.target].onRefuse(message);
                            break;
                    }
                }
                break;
//           通道销毁
            case Const.CALL_TYPE_CHANNEL_DESTROY:
                int target = channelMap.getOrDefault(message.channelId, 0);
                if (target!=0) {
                    action[target].onDestroy(message);
                    channelMap.remove(message.channelId);
                }
                break;
        }
    }

//    发送通道建立相关，因为这个信息只能发送给集群，所以target=0,初始的时候建立基础图使用
    public void sendChannelBuild(int target, int state, int errCode, int channelType) {
        Message message = Const.GetEmptyMessage();
        message.callType = Const.CALL_TYPE_CHANNEL_BUILD;
        message.state = state;
        message.sysMessage.target = target;
        message.errCode = errCode;
        message.channelType = channelType;
        doSend(message, 0);
    }



//  调度器，目标target发送信息
    public void doSend(Message message, int target) {
        try {
            channel.send(message, target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    通知周边的所有节点，添加边
    public void doNotice(int start,int end,double delay,int level){
        for(int i=0;i<addition_node.size();i++){
            action[addition_node.get(i)].notice(start,end,delay,level);
        }
        for(int i=0;i<default_neighbor.size();i++){
            action[default_neighbor.get(i)].notice(start,end,delay,level);
        }
    }


}
