package com.xm.netty_proxy_sock5Client.manager;

import com.xm.netty_proxy_sock5Client.config.Config;
import com.xm.netty_proxy_sock5Client.proxyHandler.ReturnMessageHandler;
import com.xm.netty_proxy_sock5Common.callback.ConnectCallBack;
import com.xm.netty_proxy_sock5Common.decoder.MLengthFieldBasedFrameDecoder;
import com.xm.netty_proxy_sock5Common.decoder.ProxyMessageDecoder;
import com.xm.netty_proxy_sock5Common.encoder.ProxyMessageEncoder;
import com.xm.netty_proxy_sock5Common.key.Constants;
import com.xm.netty_proxy_sock5Common.msg.ProxyMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ProxyConnectManager {

    private static Logger logger= LoggerFactory.getLogger(ProxyConnectManager.class);

    private static Bootstrap bootstrap=new Bootstrap();

    private static ConcurrentLinkedQueue<Channel> proxyChannelPool=new ConcurrentLinkedQueue<>();


    static {
        bootstrap
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                //关闭Nagle算法
                .option(ChannelOption.TCP_NODELAY, true)
                //连接超时
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline=socketChannel.pipeline();
                        pipeline.addLast(new MLengthFieldBasedFrameDecoder());
                        pipeline.addLast(new ProxyMessageEncoder());
                        pipeline.addLast(new ProxyMessageDecoder());
                        //处理返回数据
                        pipeline.addLast(new ReturnMessageHandler());
                    }
                });
    }


    public static void returnProxyConnect(Channel proxyChannel){
        if (Config.clientOpenPool){
            if (proxyChannel!=null&&proxyChannel.isActive()){
                Channel localChannel=proxyChannel.attr(Constants.NEXT_CHANNEL).get();
                if (localChannel!=null&&localChannel.isActive()){
                    localChannel.attr(Constants.NEXT_CHANNEL).set(null);
                    localChannel.close();
                }
                proxyChannel.attr(Constants.NEXT_CHANNEL).set(null);
                proxyChannelPool.offer(proxyChannel);
                logger.info("[代理池]归还代理池连接,数量->{}",proxyChannelPool.size());
            }
        }else {
            Channel localChannel=proxyChannel.attr(Constants.NEXT_CHANNEL).get();
            if (localChannel!=null&&localChannel.isActive()){
                localChannel.close();
                localChannel.attr(Constants.NEXT_CHANNEL).set(null);
            }
            proxyChannel.close();
            proxyChannel.attr(Constants.NEXT_CHANNEL).set(null);
        }
    }

    public static void getProxyConnect(ConnectCallBack connectCallBack){
        if (Config.clientOpenPool){
            Channel channel=proxyChannelPool.poll();
            if (channel!=null&&channel.isActive()){
                logger.info("[代理池]获取代理池连接");
                connectCallBack.success(channel);
            }else {
                logger.info("[代理池]创建新连接");
                bootstrap.connect(Config.serverHost,Config.serverPort).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()){
                            logger.info("[代理池]创建新连接成功");
                            connectCallBack.success(channelFuture.channel());
                        }else {
                            logger.info("[代理池]创建新连接失败");
                            connectCallBack.error();
                        }
                    }
                });
            }
        }else {
            bootstrap.connect(Config.serverHost,Config.serverPort).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()){
                        logger.info("[代理池]创建新连接成功");
                        connectCallBack.success(channelFuture.channel());
                    }else {
                        logger.info("[代理池]创建新连接失败");
                        connectCallBack.error();
                    }
                }
            });
        }
    }

    public static ProxyMessage wrapBuildConnect(String host, int port){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.BUILD_CONNECT);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost(host);
        proxyMessage.setTargetPort(port);
        proxyMessage.setData("1".getBytes());
        return proxyMessage;
    }

    public static ProxyMessage wrapClose(){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.CLOSE);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost("");
        proxyMessage.setTargetPort(8888);
        proxyMessage.setData("4".getBytes());
        return proxyMessage;
    }

    public static ProxyMessage wrapTransfer(ByteBuf byteBuf){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TRANSFER);
        proxyMessage.setUsername(Config.username);
        proxyMessage.setPassword(Config.password);
        proxyMessage.setTargetHost("3");
        proxyMessage.setTargetPort(0);
        byte[] data=new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        proxyMessage.setData(data);
        return proxyMessage;
    }
}
