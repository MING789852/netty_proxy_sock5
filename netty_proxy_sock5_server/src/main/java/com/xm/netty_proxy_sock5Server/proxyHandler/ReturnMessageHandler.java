package com.xm.netty_proxy_sock5Server.proxyHandler;

import com.xm.netty_proxy_sock5Common.key.Constants;
import com.xm.netty_proxy_sock5Server.manager.ProxyConnectManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturnMessageHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger= LoggerFactory.getLogger(ReturnMessageHandler.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
//        logger.info("[请求连接]开启，暂时不读取请求数据");
//        ctx.channel().config().setOption(ChannelOption.AUTO_READ,false);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel connectChannel=ctx.channel();
        Channel serverChannel=connectChannel.attr(Constants.NEXT_CHANNEL).get();
        //回写数据
        if (serverChannel!=null&&serverChannel.isActive()){
            ByteBuf byteBuf= (ByteBuf) msg;
            logger.info("[请求连接]回写数据大小->{}字节",byteBuf.readableBytes());
            serverChannel.writeAndFlush(ProxyConnectManager.wrapTransfer(byteBuf));
        }else {
            logger.info("[请求连接]服务连接不存在，关闭请求连接");
            connectChannel.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("[请求连接]关闭");
        Channel serverChannel=ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        ProxyConnectManager.notifyClientClose(serverChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("错误->{}",cause.getMessage());
    }
}
