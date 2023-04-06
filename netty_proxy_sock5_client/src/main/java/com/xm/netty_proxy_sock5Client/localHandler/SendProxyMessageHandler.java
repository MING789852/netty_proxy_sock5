package com.xm.netty_proxy_sock5Client.localHandler;

import com.xm.netty_proxy_sock5Client.manager.ProxyConnectManager;
import com.xm.netty_proxy_sock5Common.key.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendProxyMessageHandler extends ChannelInboundHandlerAdapter {

    protected Logger logger= LoggerFactory.getLogger(SendProxyMessageHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //暂停读取
        logger.info("暂时不读取本地数据，等服务端连接成功后再读取");
        ctx.channel().config().setOption(ChannelOption.AUTO_READ,false);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel localChannel=ctx.channel();
        Channel serverChannel=localChannel.attr(Constants.NEXT_CHANNEL).get();
        if (serverChannel!=null&&serverChannel.isActive()){
            ByteBuf byteBuf= (ByteBuf) msg;
            logger.info("发送数据大小->{}字节",byteBuf.readableBytes());
            serverChannel.writeAndFlush(ProxyConnectManager.wrapTransfer(byteBuf));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel localChannel=ctx.channel();
        Channel proxyServerChannel=localChannel.attr(Constants.NEXT_CHANNEL).get();
        if (proxyServerChannel!=null&&proxyServerChannel.isActive()){
            logger.info("本地连接关闭，通知服务端关闭连接");
            proxyServerChannel.writeAndFlush(ProxyConnectManager.wrapClose());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("错误",cause.getMessage());
    }
}
