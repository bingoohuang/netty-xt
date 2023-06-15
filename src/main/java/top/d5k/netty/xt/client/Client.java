package top.d5k.netty.xt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;
import top.d5k.netty.xt.msg.Msg;
import top.d5k.netty.xt.msg.MsgDecoder;
import top.d5k.netty.xt.msg.MsgEncoder;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Client {

    public static void main(String[] args) throws Exception {
        Client c = new Client();
        c.executor.submit(() -> c.connect("127.0.0.1", 9100));

        for (int i = 0; i < 1000; i++) {
            TimeUnit.SECONDS.sleep(3);
            try {
                c.sendMessage(("请求消息" + i).getBytes(StandardCharsets.UTF_8));
            } catch (Exception ex) {
                log.error("发消息失败", ex);
            }
        }
    }

    /**
     * 发送消息
     *
     * @param body 消息体
     */
    public void sendMessage(byte[] body) {
        Msg msg = new Msg();
        msg.setSessionID(1001L);
        msg.setPriority((byte) 1);
        msg.setType((byte) 0);
        msg.setBody(body);
        channel.writeAndFlush(msg);
    }


    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final EventLoopGroup group = new NioEventLoopGroup();

    private volatile Channel channel;

    public void connect(String host, int port) {
        // 打印资源泄露日志
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new MsgDecoder(),
                                    new MsgEncoder(),
                                    new ReadTimeoutHandler(15),
//                                    new LoginClientHandler(),
//                                    new HeartBeatClientHandler(),
                                    new ClientHandler()
                            );
                        }
                    });

            // 发起异步连接操作
            ChannelFuture future = b.connect(new InetSocketAddress(host, port)).sync();
            channel = future.channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("connect", e);
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                    channel = null;
                }
            } catch (Exception e) {
                log.error("close channel", e);
            }

            // 所有资源释放完成之后，清空资源，再次发起重连操作
            executor.schedule(() -> {
                connect(host, port);// 发起重连操作
            }, 3, TimeUnit.SECONDS);
        }
    }

}
