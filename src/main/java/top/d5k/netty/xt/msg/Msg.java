package top.d5k.netty.xt.msg;

import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public final class Msg {
    private int magic = 0xACAFDCBA; // 唯一的通信标志
    private int length; // 总消息的长度 header + body (不包括 magic 和 自身的长度）

    private byte type; // 消息的类型
    private byte tag; //  标记
    private byte priority; // 消息的优先级 0~255
    private byte version = 0; // 版本号

    private long sessionID;
    private byte[] body;

    public void setMessageType(Msg.MsgType messageType) {
        this.type = messageType.value();
    }

    public void encode(ByteBuf buf) {
        buf.writeInt(magic);
        buf.writeInt(length);

        buf.writeByte(type);
        buf.writeByte(tag);
        buf.writeByte(priority);
        buf.writeByte(version);
        buf.writeLong(sessionID);


        if (body != null && body.length > 0) {
            buf.writeBytes(body);
        }

        // 最后我们要获取整个数据包的总长度 也就是 header +  body 进行对 header length的设置

        // 在这里必须要-8个字节 ，是因为要把Magic和长度本身占的减掉了
        //（官方中给出的是：LengthFieldBasedFrameDecoder中的lengthFieldOffset+lengthFieldLength）
        // 总长度是在header协议的第二个标记字段中
        int len = buf.readableBytes() - 8;
        buf.setInt(4, len);
    }

    public void decode(ByteBuf buf) {
        magic = buf.readInt();
        length = buf.readInt();

        type = buf.readByte();
        tag = buf.readByte();
        priority = buf.readByte();
        version = buf.readByte();
        sessionID = buf.readLong();

        int dataLength = length - 12;
        body = new byte[dataLength];
        if (dataLength > 0) {
            buf.readBytes(body);
        }
    }


    public static Msg buildLoginReq() {
        Msg m = new Msg();
        m.setMessageType(MsgType.LOGIN_REQ);
        return m;
    }

    public static Msg buildLoginReq(ResultType result) {
        Msg msg = new Msg();
        msg.setMessageType(MsgType.LOGIN_RSP);
        msg.setBody(new byte[]{result.getValue()});
        return msg;
    }

    public static Msg buildHeatBeatReq() {
        Msg msg = new Msg();
        msg.setMessageType(Msg.MsgType.HEARTBEAT_REQ);
        return msg;
    }

    public static Msg buildHeatBeatRsp() {
        Msg msg = new Msg();
        msg.setMessageType(MsgType.HEARTBEAT_RSP);
        return msg;
    }

    public enum MsgType {
        /**
         * 业务请求消息
         */
        SERVICE_REQ((byte) 0),
        /**
         * 业务响应（应答）消息
         */
        SERVICE_RSP((byte) 1),
        /**
         * 业务ONE WAY消息（既是请求消息又是响应消息）
         */
        ONE_WAY((byte) 2),
        /**
         * 握手请求消息
         */
        LOGIN_REQ((byte) 3),
        /**
         * 握手响应（应答）消息
         */
        LOGIN_RSP((byte) 4),
        /**
         * 心跳请求消息
         */
        HEARTBEAT_REQ((byte) 5),
        /**
         * 心跳响应（应答）消息
         */
        HEARTBEAT_RSP((byte) 6);

        private final byte value;

        MsgType(byte value) {
            this.value = value;
        }

        public byte value() {
            return this.value;
        }
    }

}

