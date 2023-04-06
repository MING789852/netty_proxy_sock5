package com.xm.netty_proxy_sock5Common.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Sock5ProxyRequest {

    private String targetHost;
    private int targetPort;
    private ChannelHandlerContext localChannel;

    //消息队列
    private final BlockingQueue<ByteBuf> messageQueue=new LinkedBlockingQueue<>();

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public ChannelHandlerContext getLocalChannel() {
        return localChannel;
    }

    public void setLocalChannel(ChannelHandlerContext localChannel) {
        this.localChannel = localChannel;
    }

    public BlockingQueue<ByteBuf> getMessageQueue() {
        return messageQueue;
    }
}
