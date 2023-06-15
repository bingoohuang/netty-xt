package top.d5k.netty.xt.msg;

import lombok.Data;

@Data
public final class Msg {
    private Header header = new Header();
    private byte[] body;

    public static Msg buildLoginReq() {
        Header h = new Header();
        h.setMessageType(MsgType.LOGIN_REQ);

        Msg m = new Msg();
        m.setHeader(h);
        return m;
    }

    public static Msg buildLoginReq(ResultType result) {
        Header h = new Header();
        h.setMessageType(MsgType.LOGIN_RSP);

        Msg msg = new Msg();
        msg.setHeader(h);
        msg.setBody(new byte[]{result.getValue()});
        return msg;
    }

    public static Msg buildHeatBeatReq() {
        Header h = new Header();
        h.setMessageType(Msg.MsgType.HEARTBEAT_REQ);
        Msg msg = new Msg();
        msg.setHeader(h);
        return msg;
    }

    public static Msg buildHeatBeatRsp() {
        Header h = new Header();
        h.setMessageType(MsgType.HEARTBEAT_RSP);
        Msg msg = new Msg();
        msg.setHeader(h);
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

