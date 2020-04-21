//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import json.Message;
import json.config.ChannelDetialConfig;

public class Action {
    private int target;
    private double timeout;
    private int channelState;
    private int waitingCount;
    private List<Message> queue;
    private Scheduler scheduler;
    private int channelType;
    private int channelId;
    private Result result;

//    可否直接通过最短路径算法到达
    private boolean directed=true;


    public Action(int target, Scheduler scheduler) {
        this.target = target;
        this.scheduler = scheduler;
        this.timeout = 0.0D;
        this.waitingCount = 0;
        this.queue = new ArrayList<>();
        this.clearChannelInfo();
    }

    private void clearChannelInfo() {
        this.channelType =Const.CHANNEL_TYPE_ERROR;
        this.channelState = Const.CHANNEL_STATE_NONE;
        directed=true;
        this.channelId = 0;
    }

//    收到prepare信息
    public void onPrepare(Message message) {

        this.result=new Result(Main.graph.getResult(Main.config.index,message.sysMessage.target));
        directed=true;

//        路径长度太长
        int limit=(int)(Math.log(Main.config.mainConfig.nodeCount)/Math.log(2))+1;

        if(result.path.size()>=limit){
//            当前节点还没有满
            directed = false;
            if(Main.config.maxChannelConn>scheduler.channelMap.size()&&channelState==Const.CHANNEL_STATE_NONE) {
                scheduler.sendChannelBuild(message.sysMessage.target, Const.STATE_REQUEST, Const.ERR_CODE_NONE, Const.CHANNEL_TYPE_NORMAL);
                channelState=Const.CHANNEL_STATE_BUILDING;
            }else{
//                节点通道数已经满了，无法构建
                channelState=Const.CHANNEL_STATE_BUILD_FAILED;
            }
        }
    }

//    起始和终止节点

    public void passMessageDoSend(Message message) {
        if (message.sysMessage.target==Main.config.index) {
//            终点情况
            System.out.println("succ received message: " + message.sysMessage.data);
            return;
        }
//       可以直接用最短路径
        if(directed){
            message.extMessage.put("final_target", "" + message.sysMessage.target);
            message.extMessage.put("cur_index", "0");
            String path = "";
            int i;
            for (Iterator var3 = this.result.path.iterator(); var3.hasNext(); path = path + i + "_") {
                i = (Integer) var3.next();
            }
            message.extMessage.put("path", path);
            message.extMessage.put("directed","1");
            direct_passMessage(message);
            return;
        }

//        消息可能可以直接传递
        if(channelState!=Const.CHANNEL_STATE_BUILD_FAILED){
            if(channelState==Const.CHANNEL_STATE_BUILD_FINISH){
                message.channelId=this.channelId;
                this.scheduler.doSend(message,message.sysMessage.target);
            }else if(channelState==Const.CHANNEL_STATE_BUILDING){
                queue.add(message);
            }
            return;
        }
        jump(message);

    }

    public void jump(Message message){
        result=new Result(Main.graph.getResult(Main.config.index,message.sysMessage.target));

        int nextClient=result.path.get(1);
        message.callType="sys";
        message.extMessage.put("final_target", "" + message.sysMessage.target);
        message.extMessage.put("jump","1");
        message.channelId=this.scheduler.action[nextClient].channelId;

        if(nextClient==Integer.parseInt(message.extMessage.get("final_target"))){
            message.callType=Const.CALL_TYPE_SEND;
            this.scheduler.doSend(message,nextClient);
        }else{
            this.scheduler.doSend(message,nextClient);
        }

    }


//    节点之间消息互传,路径长度小于5,直接传输
    public void direct_passMessage(Message message) {
        message.callType = "sys";
        int nextIndex = Integer.parseInt((String)message.extMessage.get("cur_index")) + 1;
        String[] path = ((String)message.extMessage.get("path")).split("_");
        int nextClient = Integer.parseInt(path[nextIndex]);
        message.channelId = this.scheduler.action[nextClient].channelId;
        if (nextClient == Integer.parseInt((String)message.extMessage.get("final_target"))) {
            message.callType = "send";
            if(message.channelId==0){
                this.scheduler.action[nextClient].queue.add(message);
                return;
            }
            this.scheduler.doSend(message, nextClient);
        } else {
            message.extMessage.put("cur_index", "" + nextIndex);
            if(message.channelId==0){
                this.scheduler.action[nextClient].queue.add(message);
                return;
            }
            this.scheduler.doSend(message, nextClient);
        }
    }


//


//    通道建立成功之后
    public void onSucc(Message message) {


        this.channelType = message.channelType;
        this.channelState = Const.CHANNEL_STATE_BUILD_FINISH;
        this.channelId = message.channelId;


//        是后面添加的路径
        if(!scheduler.default_neighbor.contains(message.sysMessage.target)){

            /*****************************************************************额外通道*************************************************************/
            System.out.println(message.sysMessage.target);
            System.out.println(Main.config.index);
            /*****************************************************************额外通道*************************************************************/


            System.out.println("*****************************before graph size************************************************************"+Util.getGraphSize(Main.graph));
            Util.addEdge_NoWeight(Main.graph,Main.config.index,message.sysMessage.target);
            System.out.println("*****************************after graph size************************************************************"+Util.getGraphSize(Main.graph));

//            TODO 通知周边节点，添加边

            scheduler.doNotice(Main.config.index,message.sysMessage.target,
                    message.channelType==1?Main.config.channelConfig.highSpeed.lag:Main.config.channelConfig.normalSpeed.lag+1,1);

            scheduler.addition_node.add(message.sysMessage.target);
        }
//        清除过期信息
//        this.filterQueue();
        for(Message msg:queue){
            this.scheduler.doSend(msg,msg.sysMessage.target);
        }
        this.queue.clear();
    }

    public void notice(int start,int end,double delay,int level){
        Message message=Const.GetEmptyMessage();
        message.channelId=channelId;
        message.callType="sys";
        message.extMessage.put("notice",""+level);
        message.extMessage.put("start",""+start);
        message.extMessage.put("end",""+end);
        message.extMessage.put("delay",""+delay);
        message.channelType=this.channelType;

        scheduler.doSend(message,target);
    }

//    收到通道建立请求
    public void onRequest(Message message) {
        int target = message.sysMessage.target;
        if (this.channelState != 0 && this.scheduler.channelMap.size() == Main.config.maxChannelConn) {
//            拒绝建立通道,当前节点通道已经满了
            this.scheduler.sendChannelBuild(target, Const.STATE_REFUSE, Const.ERR_CODE_CHANNEL_BUILD_TARGET_REFUSE, message.channelType);
        } else {
            this.scheduler.sendChannelBuild(target, Const.STATE_ACCEPT, Const.ERR_CODE_NONE, message.channelType);
            this.channelType = message.channelType;
            this.channelState = Const.CHANNEL_STATE_BUILD_FINISH;
        }
    }

//    收到拒绝建立通道请求
    public void onRefuse(Message message) {
//        对面链接已经满了
//            通道建立失败，将队列中的消息跳转出去
        for(Message ms:queue){
            jump(ms);
        }
        channelState= Const.CHANNEL_STATE_BUILD_FAILED;
    }

//    发送信息
    public void doSend(Message message) {
        if (message.recvTime + (double)Main.config.mainConfig.timeOut >= Main.curTime() + (double)this.getConfig().lag) {
            message.channelId = this.channelId;
            this.scheduler.doSend(message, message.sysMessage.target);
        }

    }



    public boolean isValid() {
        return this.channelType != -1 && (this.channelState == 2 || this.channelState == 3);
    }

    public int getOtherType() {
        return this.channelType == 0 ? 1 : 0;
    }

    public void doRequest(int channelType) {
        this.channelType = channelType;
        this.channelState = 1;
        this.scheduler.sendChannelBuild(this.target, 0, 0, channelType);
    }




    public void onDestroy(Message message) {
        if (this.channelState == 3) {
            System.out.println("on destroy");
            this.clearChannelInfo();
        }

        this.filterQueue();
        if (this.waitingCount > 0 || this.queue.size() > 0) {
            this.doRequest(this.getOtherType());
        }

    }

    public void doDestroy(int channelId) {
        Message message = Const.GetEmptyMessage();
        message.callType = "channel_destroy";
        message.channelId = channelId;
        message.state = 0;
        this.scheduler.doSend(message, 0);
    }







    public void onSend(Message message) {
        if (this.scheduler.getId() == message.sysMessage.target) {
            System.out.println("succ received message: " + message.sysMessage.data);
        } else {
            --this.waitingCount;
            if (this.channelState != 3 && this.channelState != 2) {
                System.out.println("add into cache");
                System.out.printf("channelState:%d\n", this.channelState);
                this.queue.add(message);
            } else {
                System.out.println("send directory");
                this.doSend(message);
                this.queue.forEach((msg) -> {
                    this.doSend(msg);
                });
                this.channelState = 3;
            }

        }
    }



    public void filterQueue() {
        ArrayList<Message> filtered = new ArrayList();
        Iterator var2 = this.queue.iterator();

        while(var2.hasNext()) {
            Message message = (Message)var2.next();
            ChannelDetialConfig selfConf = this.getConfig();
            float lag = selfConf == null ? 0.0F : selfConf.lag;
            if (message.recvTime + (double)Main.config.mainConfig.timeOut >= Main.curTime() + (double)lag) {
                filtered.add(message);
            }
        }

        this.queue = filtered;
    }

    public ChannelDetialConfig getConfig() {
        switch(this.channelType) {
            case 0:
                return Main.config.channelConfig.normalSpeed;
            case 1:
                return Main.config.channelConfig.highSpeed;
            default:
                return null;
        }
    }
}
