package com.xm.netty_proxy_sock5Client.boot;

import com.xm.netty_proxy_sock5Client.config.Config;
import com.xm.netty_proxy_sock5Client.localHandler.SendProxyMessageHandler;
import com.xm.netty_proxy_sock5Client.localHandler.Socks5CommandRequestHandler;
import com.xm.netty_proxy_sock5Client.localHandler.Socks5InitialRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalSock5ClientBoot {
    protected static Logger logger= LoggerFactory.getLogger(LocalSock5ClientBoot.class);

    private EventLoopGroup bossGroup=new NioEventLoopGroup();
    private EventLoopGroup workGroup=new NioEventLoopGroup();

    private int port;

    public LocalSock5ClientBoot(int port){
        this.port=port;
    }

    public void run(){
        try {
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline =socketChannel.pipeline();
                            pipeline.addLast(Socks5ServerEncoder.DEFAULT);
                            //初始化
                            pipeline.addLast(new Socks5InitialRequestDecoder());
                            pipeline.addLast(new Socks5InitialRequestHandler());
                            //处理连接
                            pipeline.addLast(new Socks5CommandRequestDecoder());
                            pipeline.addLast(new Socks5CommandRequestHandler());
                        }
                    });
            logger.debug("bind port : " + port);
            ChannelFuture future = serverBootstrap.bind(port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        LocalSock5ClientBoot localSock5ClientBoot=new LocalSock5ClientBoot(Config.clientPort);
        localSock5ClientBoot.run();
    }
}
