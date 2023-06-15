package top.d5k.netty.xt.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.d5k.netty.xt.msg.Msg;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    private volatile ScheduledFuture<?> heartBeat;

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Msg m = (Msg) msg;
        // 握手成功，主动发送心跳消息
        byte typ = m.getType();
        if (typ == Msg.MsgType.LOGIN_RSP.value()) {
            heartBeat = ctx.executor().scheduleAtFixedRate(new Task(ctx), 0, 15, TimeUnit.SECONDS);
        } else if (typ == Msg.MsgType.HEARTBEAT_RSP.value()) {
            log.info("got heart beat: {}", m);
        } else
            ctx.fireChannelRead(msg);
    }

    /**
     * 发送心跳消息的任务线程
     *
     * @author landyChris
     */
    @AllArgsConstructor
    private static class Task implements Runnable {
        private final ChannelHandlerContext ctx;

        public void run() {
            Msg heatBeat = Msg.buildHeatBeatReq();
            ctx.writeAndFlush(heatBeat);
        }

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught", cause);

        // 断连期间，心跳定时器停止工作，不再发送心跳请求信息
        if (heartBeat != null) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }
}
