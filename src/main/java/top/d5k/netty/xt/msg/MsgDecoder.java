package top.d5k.netty.xt.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * LengthFieldBasedFrameDecoder 是为了解决 拆包粘包等问题的
 */
public class MsgDecoder extends LengthFieldBasedFrameDecoder {
    public MsgDecoder() {
        // lengthFieldOffset 长度属性的偏移量 简单来说就是 message中 总长度的起始位置（Header中的length属性的起始位置）
        // lengthFieldLength 属性的长度 整个属性占多长（length属性为int，占4个字节）
        super(1024 * 1024 * 5, 4, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 1 调用父类(LengthFieldBasedFrameDecoder)方法:
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        Msg m = new Msg();
        m.decode(frame);

        return m;
    }
}
