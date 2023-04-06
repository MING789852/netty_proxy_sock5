package com.xm.netty_proxy_sock5Client.proxyHandler;

import com.xm.netty_proxy_sock5Client.localHandler.SendProxyMessageHandler;
import com.xm.netty_proxy_sock5Client.manager.ProxyConnectManager;
import com.xm.netty_proxy_sock5Common.key.Constants;
import com.xm.netty_proxy_sock5Common.msg.ProxyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturnMessageHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private static Logger logger= LoggerFactory.getLogger(ReturnMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
        Channel proxyChannel=channelHandlerContext.channel();
        Channel localChannel=proxyChannel.attr(Constants.NEXT_CHANNEL).get();
        if (localChannel!=null&&localChannel.isActive()){
            if (ProxyMessage.CONNECT_SUCCESS==proxyMessage.getType()){
                localChannel.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4)).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()){
                            logger.info("连接成功->{},{},开始读取本地数据",proxyMessage.getTargetHost(),proxyMessage.getTargetPort());
                            //开始读取数据
                            localChannel.config().setOption(ChannelOption.AUTO_READ,true);
                            //发送数据
                            localChannel.pipeline().addLast(new SendProxyMessageHandler());
                        }
                    }
                });
            }
            if (ProxyMessage.TRANSFER==proxyMessage.getType()){
                if (localChannel!=null&&localChannel.isActive()){
                    ByteBuf byteBuf = channelHandlerContext.alloc().buffer(proxyMessage.getData().length);
                    logger.info("回写数据大小->{}字节",proxyMessage.getData().length);
                    byteBuf.writeBytes(proxyMessage.getData());
                    localChannel.writeAndFlush(byteBuf);
                }
            }
            if (ProxyMessage.CLOSE==proxyMessage.getType()){
                logger.info("收到服务端关闭通知，关闭本地连接,归还代理连接");
                //归还代理连接
                ProxyConnectManager.returnProxyConnect(proxyChannel);
                //关闭本地连接
                if (localChannel.isActive()){
                    localChannel.close();
                }
            }
        }else {
            ProxyConnectManager.returnProxyConnect(proxyChannel);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("服务连接关闭");
        Channel localChannel=ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if (localChannel!=null&&localChannel.isActive()){
            localChannel.attr(Constants.NEXT_CHANNEL).set(null);
            localChannel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("错误",cause.getMessage());
    }
}
