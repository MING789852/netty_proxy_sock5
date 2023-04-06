package com.xm.netty_proxy_sock5Client.localHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;

public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DefaultSocks5InitialRequest defaultSocks5InitialRequest) throws Exception {
        if (defaultSocks5InitialRequest.decoderResult().isSuccess()){
            DefaultSocks5InitialResponse response=new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
            channelHandlerContext.writeAndFlush(response);
        }
    }
}
