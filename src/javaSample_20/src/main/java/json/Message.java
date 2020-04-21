package json;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.Map;

public class Message implements Cloneable{
    public String callType;
    public int channelId;
    public SysMessage sysMessage;

//    final_target
//    cur_index
//    path 用下划线分隔_
//    add_connect
//    added_node
//    directed

//    notice
//    start
//    end
//    delay

    public Map<String, String> extMessage;


    public int state;
    public int errCode;
    public int channelType;
    public int targetId;

    @JsonIgnore
    public double recvTime;

    public Message() {
        recvTime = new Date().getTime() / 1000.0;
    }

    @Override
    public Message clone() throws CloneNotSupportedException {
        return (Message) super.clone();
    }
}
