package top.d5k.netty.xt.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import top.d5k.netty.xt.msg.Msg;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Msg record = (Msg) msg;
        log.info("got msg: {}", new String(record.getBody(), StandardCharsets.UTF_8));
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}