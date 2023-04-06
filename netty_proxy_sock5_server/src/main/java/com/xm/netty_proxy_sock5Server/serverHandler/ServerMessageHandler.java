package com.xm.netty_proxy_sock5Server.serverHandler;

import com.xm.netty_proxy_sock5Common.callback.ConnectCallBack;
import com.xm.netty_proxy_sock5Common.key.Constants;
import com.xm.netty_proxy_sock5Common.msg.ProxyMessage;
import com.xm.netty_proxy_sock5Server.config.Config;
import com.xm.netty_proxy_sock5Server.manager.ProxyConnectManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMessageHandler extends SimpleChannelInboundHandler<ProxyMessage> {
    protected static Logger logger= LoggerFactory.getLogger(ServerMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
        Channel serverChannel=channelHandlerContext.channel();
        //验证账号密码是否正确
        if (Config.username.equals(proxyMessage.getUsername())&&Config.password.equals(proxyMessage.getPassword())){
            if (ProxyMessage.BUILD_CONNECT==proxyMessage.getType()){
                ProxyConnectManager.connect(proxyMessage.getTargetHost(), proxyMessage.getTargetPort(), serverChannel, new ConnectCallBack() {
                    @Override
                    public void success(Channel connectChannel) {
                        //绑定连接
                        serverChannel.attr(Constants.NEXT_CHANNEL).set(connectChannel);
                        connectChannel.attr(Constants.NEXT_CHANNEL).set(serverChannel);
                        //发送连接成功回调
                        serverChannel.writeAndFlush(ProxyConnectManager.wrapConnectSuccess(proxyMessage.getTargetHost(),proxyMessage.getTargetPort()));
                    }

                    @Override
                    public void error() {
                        //通知客户端关闭连接
                        ProxyConnectManager.notifyClientClose(serverChannel);
                    }
                });
            }
            if (ProxyMessage.TRANSFER==proxyMessage.getType()){
                Channel connectChannel=serverChannel.attr(Constants.NEXT_CHANNEL).get();
                if (connectChannel!=null&&connectChannel.isActive()){
//                    connectChannel.config().setOption(ChannelOption.AUTO_READ,true);
                    ByteBuf byteBuf = channelHandlerContext.alloc().buffer(proxyMessage.getData().length);
                    byteBuf.writeBytes(proxyMessage.getData());
                    logger.info("[转发数据]发送数据大小->{}字节", proxyMessage.getData().length);
                    connectChannel.writeAndFlush(byteBuf);
                }else {
                    //通知客户端关闭连接
                    ProxyConnectManager.notifyClientClose(serverChannel);
                }
            }
            if (ProxyMessage.CLOSE==proxyMessage.getType()){
                //关闭连接
                ProxyConnectManager.notifyClientClose(serverChannel);
            }
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel serverChannel=ctx.channel();
        logger.info("[关闭服务连接]服务channelId->{}",serverChannel.id().asShortText());
        Channel connectChannel=serverChannel.attr(Constants.NEXT_CHANNEL).get();
        if (connectChannel!=null&&connectChannel.isActive()){
            connectChannel.attr(Constants.NEXT_CHANNEL).set(null);
            connectChannel.close();
        }
        serverChannel.attr(Constants.NEXT_CHANNEL).set(null);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("错误->{}",cause.getMessage());
    }
}
