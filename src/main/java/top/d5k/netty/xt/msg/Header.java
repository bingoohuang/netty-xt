package top.d5k.netty.xt.msg;

import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Data
public class Header {
    private int magic = 0xACAFDCBA; // 唯一的通信标志
    private int length; // 总消息的长度 header + body
    private long sessionID; // 会话ID
    private byte type; // 消息的类型
    private byte priority; // 消息的优先级 0~255
    private Map<String, byte[]> attachment = new HashMap<>(); // 附件

    public void setMessageType(Msg.MsgType messageType) {
        this.type = messageType.value();
    }

    public void encode(ByteBuf buf) {
        buf.writeInt(magic);
        buf.writeInt(length);
        buf.writeLong(sessionID);
        buf.writeByte(type);
        buf.writeByte(priority);

        buf.writeInt((attachment.size()));
        for (Map.Entry<String, byte[]> p : attachment.entrySet()) {
            byte[] k = p.getKey().getBytes(StandardCharsets.UTF_8);
            buf.writeInt(k.length);
            buf.writeBytes(k);
            byte[] v = p.getValue();
            buf.writeInt(v.length);
            buf.writeBytes(v);
        }
    }

    public void decode(ByteBuf buf) {
        magic = buf.readInt();
        length = buf.readInt();
        sessionID = buf.readLong();
        type = buf.readByte();
        priority = buf.readByte();

        int attachSize = buf.readInt();
        if (attachSize > 0) {
            Map<String, byte[]> attach = new HashMap<>(attachSize);
            for (int i = 0; i < attachSize; i++) {
                int keySize = buf.readInt();
                byte[] keyArray = new byte[keySize];
                buf.readBytes(keyArray);
                String key = new String(keyArray, StandardCharsets.UTF_8);
                int valSize = buf.readInt();
                byte[] valArray = new byte[valSize];
                buf.readBytes(valArray);
                attach.put(key, valArray);
            }
            attachment = attach;
        }
    }
}
