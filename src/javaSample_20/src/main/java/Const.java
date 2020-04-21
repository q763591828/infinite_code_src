import com.fasterxml.jackson.databind.ObjectMapper;
import json.Message;

public class Const {
    public final static String CALL_TYPE_PREPARE = "prepare";
    public final static String CALL_TYPE_SEND = "send";
    public final static String CALL_TYPE_SYS = "sys";
    public final static String CALL_TYPE_CHANNEL_BUILD = "channel_build";
    public final static String CALL_TYPE_CHANNEL_DESTROY = "channel_destroy";

    public final static int STATE_REQUEST = 0;
    public final static int STATE_ACCEPT = 1;
    public final static int STATE_REFUSE = 2;
    public final static int STATE_NOTICE = -1;

    public final static int CHANNEL_TYPE_ERROR = -1;
    public final static int CHANNEL_TYPE_NORMAL = 0;
    public final static int CHANNEL_TYPE_FAST = 1;

    public final static int ERR_CODE_NONE = 0x0;
    public final static int ERR_CODE_NO_SUCH_CHANNEL = 0x001;
    public final static int ERR_CODE_NO_SUCH_CALL_TYPE = 0x002;
    public final static int ERR_CODE_CHANNEL_BUILD_MASK = 0x0100;
    public final static int ERR_CODE_CHANNEL_BUILD_TARGET_REFUSE = 0x0101;
    public final static int ERR_CODE_CHANNEL_BUILD_TARGET_LIMIT = 0x0102;
    public final static int ERR_CODE_CHANNEL_BUILD_TOTAL_LIMIT = 0x0103;
    public final static int ERR_CODE_CHANNEL_BUILD_SOURCE_LIMIT = 0x0104;
    public final static int ERR_CODE_CHANNEL_BUILD_TARGET_TIMEOUT = 0x0105;
    public final static int ERR_CODE_CHANNEL_BUILD_UNKNOWN_OPERATION = 0x106;
    public final static int ERR_CODE_SEND_MASK = 0x200;
    public final static int ERR_CODE_SEND_COUNT_LIMIT = 0x201;
    public final static int ERR_CODE_SEND_SIZE_LIMIT = 0x202;

    /**
     *     // 没有错误
     *     const int ERR_CODE_NONE = 0x0;
     *     // 通道不存在
     *     const int ERR_CODE_NO_SUCH_CHANNEL = 0x001;
     *     // call type 不存在
     *     const int ERR_CODE_NO_SUCH_CALL_TYPE = 0x002;
     *     // 通道对面不是这个节点
     *     const int ERR_CODE_PORT_NOT_VALID = 0x003;
     *     // 通道创建相关
     *     const int ERR_CODE_CHANNEL_BUILD_MASK = 0x0100;
     *     // 对方拒绝创建
     *     const int ERR_CODE_CHANNEL_BUILD_TARGET_REFUSE = 0x0101;
     *     // 对方连接数达到上限
     *     const int ERR_CODE_CHANNEL_BUILD_TARGET_LIMIT = 0x0102;
     *     // 总连接数达到上限
     *     const int ERR_CODE_CHANNEL_BUILD_TOTAL_LIMIT = 0x0103;
     *
     *     // 自己节点数达到上限
     *     const int ERR_CODE_CHANNEL_BUILD_SOURCE_LIMIT = 0x0104;
     *     // 对面超时没响应
     *     const int ERR_CODE_CHANNEL_BUILD_TARGET_TIMEOUT = 0x0105;
     *     // 未知操作
     *     const int ERR_CODE_CHANNEL_BUILD_UNKNOWN_OPERATION = 0x106;
     *     // 发送时相关
     *     const int ERR_CODE_SEND_MASK = 0x200;
     *     // 通道上的消息数达到上限
     *     const int ERR_CODE_SEND_COUNT_LIMIT = 0x201;
     *     // extra字段大小达到上限
     *     const int ERR_CODE_SEND_SIZE_LIMIT = 0x202;
     */

    public final static double EXP = 1e-5;

    public final static ObjectMapper MAPPER = new ObjectMapper();
    public final static String EMPTY_MESSAGE = "{\"callType\": \"\",\"channelId\": 0,\"sysMessage\": {\"target\": 0,\"data\": \"\",\"delay\": 0.0},\"extMessage\": {},\"state\": 0,\"errCode\": 0,\"channelType\": 0}";
    public static Message GetEmptyMessage() {
        try {
            return MAPPER.readValue(EMPTY_MESSAGE, Message.class);
        } catch (Exception e) {
            return null;
        }
    }

//    默认状态
    public final static int CHANNEL_STATE_NONE = 0;
    public final static int CHANNEL_STATE_BUILDING=1;
    public final static int CHANNEL_STATE_BUILD_FINISH=2;
    public final static int CHANNEL_STATE_BUILD_FAILED=3;
}
