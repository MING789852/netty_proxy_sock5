package com.xm.netty_proxy_sock5Client.localHandler;

import com.xm.netty_proxy_sock5Client.manager.ProxyConnectManager;
import com.xm.netty_proxy_sock5Common.callback.ConnectCallBack;
import com.xm.netty_proxy_sock5Common.key.Constants;
import com.xm.netty_proxy_sock5Common.msg.Sock5ProxyRequest;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    protected static Logger logger= LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DefaultSocks5CommandRequest defaultSocks5CommandRequest) throws Exception {
        if (defaultSocks5CommandRequest.decoderResult().isSuccess()){
            if (Socks5CommandType.CONNECT.equals(defaultSocks5CommandRequest.type())) {
                logger.debug("连接->{}",defaultSocks5CommandRequest);

                Channel localChannel=channelHandlerContext.channel();

                //设置代理信息
                Sock5ProxyRequest request=new Sock5ProxyRequest();
                request.setTargetHost(defaultSocks5CommandRequest.dstAddr());
                request.setTargetPort(defaultSocks5CommandRequest.dstPort());
                request.setLocalChannel(channelHandlerContext);

                ProxyConnectManager.getProxyConnect(new ConnectCallBack() {
                    @Override
                    public void success(Channel proxyServerChannel) {
                        //绑定连接
                        localChannel.attr(Constants.NEXT_CHANNEL).set(proxyServerChannel);
                        proxyServerChannel.attr(Constants.NEXT_CHANNEL).set(localChannel);
                        //发送建立连接请求
                        proxyServerChannel.writeAndFlush(ProxyConnectManager.wrapBuildConnect(request.getTargetHost(),request.getTargetPort()));
                    }
                    @Override
                    public void error() {
                        logger.error("连接代理服务器失败");
                        localChannel.close();
                    }
                });
            }else {
                channelHandlerContext.fireChannelRead(defaultSocks5CommandRequest);
            }
        }
    }
}
