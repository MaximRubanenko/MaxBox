package ru.max.client;

import handlers.HandlerRegistry;
import handlers.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;


@Data

public class MainHandler extends ChannelInboundHandlerAdapter {

    private String userName;
    private HandlerRegistry handlerRegistry;

    public MainHandler(Controller controller) {
        this.handlerRegistry = new HandlerRegistry(controller);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        RequestHandler handler = handlerRegistry.getHandler(msg.getClass());
        handler.handle(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
