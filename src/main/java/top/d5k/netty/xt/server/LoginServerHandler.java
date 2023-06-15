package top.d5k.netty.xt.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import top.d5k.netty.xt.msg.Header;
import top.d5k.netty.xt.msg.Msg;
import top.d5k.netty.xt.msg.ResultType;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LoginServerHandler extends ChannelInboundHandlerAdapter {
    private final Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();
    private final Map<String, Boolean> whiteList = new HashMap<String, Boolean>() {{
        put("127.0.0.1", Boolean.TRUE);
    }};


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Msg m = (Msg) msg;

        // 如果是握手请求消息，处理，其它消息透传
        Header h = m.getHeader();
        if (h == null || h.getType() != Msg.MsgType.LOGIN_REQ.value()) {
            ctx.fireChannelRead(msg);
            return;
        }


        String nodeIndex = ctx.channel().remoteAddress().toString();
        Msg loginResp;
        if (nodeCheck.containsKey(nodeIndex)) {
            log.error("重复登录,拒绝请求!");
            loginResp = Msg.buildLoginReq(ResultType.FAIL);
        } else {
            InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
            String ip = addr.getAddress().getHostAddress();
            boolean isOK = whiteList.containsKey(ip);
            loginResp = Msg.buildLoginReq(isOK ? ResultType.SUCCESS : ResultType.FAIL);
            if (isOK) {
                nodeCheck.put(nodeIndex, true);
            }
        }

        log.info("The login rsp is : {} body [{}]", loginResp, loginResp.getBody());
        ctx.writeAndFlush(loginResp);
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        nodeCheck.remove(ctx.channel().remoteAddress().toString());// 删除缓存
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
