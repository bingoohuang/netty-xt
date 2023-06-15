package top.d5k.netty.xt.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import top.d5k.netty.xt.msg.Header;
import top.d5k.netty.xt.msg.Msg;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当我们通道进行激活的时候 触发的监听方法
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelActive");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive");
        super.channelInactive(ctx);
    }

    /**
     * 当我们的通道里有数据进行读取的时候 触发的监听方法
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Msg requestMsg = (Msg) msg;

        String body = new String(requestMsg.getBody(), StandardCharsets.UTF_8);
        log.info("recv message {}", body);

        Msg rsp = new Msg();
        Header header = new Header();
        header.setSessionID(2002L);
        header.setPriority((byte) 2);
        header.setType((byte) 1);
        rsp.setHeader(header);
        rsp.setBody(("我是响应数据: " + body).getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(rsp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // 数据读取完毕
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
