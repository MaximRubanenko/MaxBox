package ru.max.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import lombok.Data;
import messages.AbstractMessage;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

@Data
public class Network implements Runnable {

    private final String HOST = "localhost";  //192.168.1.205";localhost
    private final int PORT = 8188;
    private Controller controller;
    private Channel currentChannel;
    private CountDownLatch countDownLatch;

    public Network(Controller controller, CountDownLatch countDownLatch) {
        this.controller = controller;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        openConnection();
    }

    public void openConnection() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(HOST, PORT))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(1024 * 1024 * 100, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler(controller)
                            );
                            currentChannel = socketChannel;
                        }
                    });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ChannelFuture send(AbstractMessage msg){
        return currentChannel.writeAndFlush(msg);
    }

    public void close() {
        currentChannel.close();
    }
}
