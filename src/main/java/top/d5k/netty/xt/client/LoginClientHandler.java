package top.d5k.netty.xt.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import top.d5k.netty.xt.msg.Header;
import top.d5k.netty.xt.msg.Msg;
import top.d5k.netty.xt.msg.ResultType;

@Slf4j
public class LoginClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channelActive");
        ctx.writeAndFlush(Msg.buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Msg m = (Msg) msg;

        // 如果是握手应答消息，需要判断是否认证成功
        Header h = m.getHeader();
        if (h == null || h.getType() != Msg.MsgType.LOGIN_RSP.value()) {
            ctx.fireChannelRead(msg);
            return;
        }

        byte loginResult = m.getBody()[0];
        if (loginResult != ResultType.SUCCESS.getValue()) {
            log.info("login failed");
            ctx.close();
        } else {
            log.info("Login ok");
            ctx.fireChannelRead(msg);
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught", cause);
        ctx.fireExceptionCaught(cause);
    }
}
