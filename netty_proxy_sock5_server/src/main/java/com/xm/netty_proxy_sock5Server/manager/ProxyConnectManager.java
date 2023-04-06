package com.xm.netty_proxy_sock5Server.manager;

import com.xm.netty_proxy_sock5Common.callback.ConnectCallBack;
import com.xm.netty_proxy_sock5Common.key.Constants;
import com.xm.netty_proxy_sock5Common.msg.ProxyMessage;
import com.xm.netty_proxy_sock5Server.config.Config;
import com.xm.netty_proxy_sock5Server.proxyHandler.ReturnMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyConnectManager {
    protected static Logger logger= LoggerFactory.getLogger(ProxyConnectManager.class);

    private static Bootstrap bootstrap=new Bootstrap();

    static {
        bootstrap
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                //关闭Nagle算法
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline=socketChannel.pipeline();
                        pipeline.addLast(new ReturnMessageHandler());
                    }
                });
    }

    public static void connect(String host, int port, Channel serverChannel, ConnectCallBack connectCallBack) {
        String serverChannelId=serverChannel.id().asShortText();
        bootstrap.connect(host,port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (channelFuture.isSuccess()){
                    //添加数据回调处理
                    logger.info("服务channelId->{},连接成功->{},{}",serverChannelId,host,port);
                    connectCallBack.success(channelFuture.channel());
                }else {
                    logger.info("服务channelId->{},连接失败->{},{}",serverChannelId,host,port);
                    connectCallBack.error();
                }
            }
        });
    }

    public static  void  notifyClientClose(Channel serverChannel) {
        if (serverChannel!=null&&serverChannel.isActive()) {
            logger.info("通知客户端关闭连接");
            Channel connectChannel = serverChannel.attr(Constants.NEXT_CHANNEL).get();
            if (connectChannel != null && connectChannel.isActive()) {
                connectChannel.close();
                connectChannel.attr(Constants.NEXT_CHANNEL).set(null);
            }
            serverChannel.attr(Constants.NEXT_CHANNEL).set(null);
            serverChannel.writeAndFlush(ProxyConnectManager.wrapClose());
        }
    }

    public static ProxyMessage wrapClose(){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.CLOSE);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost("4");
        proxyMessage.setTargetPort(4);
        proxyMessage.setData("4".getBytes());

        return proxyMessage;
    }

    public static ProxyMessage wrapConnectSuccess(String host,int port){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.CONNECT_SUCCESS);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost(host);
        proxyMessage.setTargetPort(port);
        proxyMessage.setData("2".getBytes());

        return proxyMessage;
    }

    public static ProxyMessage wrapTransfer(ByteBuf byteBuf){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TRANSFER);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost("3");
        proxyMessage.setTargetPort(8888);
        byte[] data=new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        proxyMessage.setData(data);
        return proxyMessage;
    }
}
