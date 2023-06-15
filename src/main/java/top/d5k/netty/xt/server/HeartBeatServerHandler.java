package top.d5k.netty.xt.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import top.d5k.netty.xt.msg.Msg;

/**
 * 由于采用长连接通信，在正常业务运行期间，双方通过心跳和业务消息维持链路，任何一方都不需要主动关闭链接。<br/>
 * 但是在以下情况，客户端和服务端都需要关闭连接。<br/>
 * 1.当对方宕机或者重启时，会主动关闭链接，另一方读取到操作系统的通知信号，得知对方REST链路，需要关闭连接，释放自身的句柄等资源，<br/>
 * 由于采用全双工通信，双方都需要关闭连接，节省资源<br/>
 * 2.消息读写过程中发生IO异常，需要主动关闭连接。<br/>
 * 3.心跳消息读写过程发送IO异常，需要主动关闭连接。<br/>
 * 4.心跳超时，需要主动关闭连接。<br/>
 * 5.发生编码异常，需要主动关闭连接。<br/>
 */
@Slf4j
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Msg h = (Msg) msg;
        if (h.getType() == Msg.MsgType.HEARTBEAT_REQ.value()) {
            log.info("got heart beat: {} ", h);
            Msg heartBeat = Msg.buildHeatBeatRsp();
            ctx.writeAndFlush(heartBeat);
        } else
            ctx.fireChannelRead(msg);
    }
}
