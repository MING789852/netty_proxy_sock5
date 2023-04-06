package com.xm.netty_proxy_sock5Server.boot;

import com.xm.netty_proxy_sock5Common.decoder.MLengthFieldBasedFrameDecoder;
import com.xm.netty_proxy_sock5Common.decoder.ProxyMessageDecoder;
import com.xm.netty_proxy_sock5Common.encoder.ProxyMessageEncoder;
import com.xm.netty_proxy_sock5Server.config.Config;
import com.xm.netty_proxy_sock5Server.serverHandler.ServerMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxySock5ServerBoot {

    protected static Logger logger= LoggerFactory.getLogger(ProxySock5ServerBoot.class);

    private EventLoopGroup bossGroup=new NioEventLoopGroup();
    private EventLoopGroup workerGroup=new NioEventLoopGroup();
    private ServerBootstrap bootstrap=new ServerBootstrap();

    private int port;

    public ProxySock5ServerBoot(int port) {
        this.port = port;
    }


    public void run(){
        try {
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                             ChannelPipeline pipeline =socketChannel.pipeline();
                             pipeline.addLast(new MLengthFieldBasedFrameDecoder());
                             pipeline.addLast(new ProxyMessageDecoder());
                             pipeline.addLast(new ProxyMessageEncoder());
                             //处理数据
                             pipeline.addLast(new ServerMessageHandler());
                        }
                    });
            logger.debug("bind port : " + port);
            ChannelFuture future = bootstrap.bind(port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        ProxySock5ServerBoot proxySock5ServerBoot=new ProxySock5ServerBoot(Config.serverPort);
        proxySock5ServerBoot.run();
    }
}
