package top.d5k.netty.xt.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgEncoder extends MessageToByteEncoder<Msg> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Msg msg, ByteBuf sendBuf) {
        msg.getHeader().encode(sendBuf);

        byte[] body = msg.getBody();
        if (body != null) {
            sendBuf.writeInt(body.length);
            sendBuf.writeBytes(body);
        } else {
            sendBuf.writeInt(0);
        }

        // 最后我们要获取整个数据包的总长度 也就是 header +  body 进行对 header length的设置

        // 在这里必须要-8个字节 ，是因为要把Magic和长度本身占的减掉了
        //（官方中给出的是：LengthFieldBasedFrameDecoder中的lengthFieldOffset+lengthFieldLength）
        // 总长度是在header协议的第二个标记字段中
        int len = sendBuf.readableBytes() - 8;
        sendBuf.setInt(4, len);
    }
}
