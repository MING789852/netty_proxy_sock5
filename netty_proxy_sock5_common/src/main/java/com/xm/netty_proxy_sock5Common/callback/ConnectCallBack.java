package com.xm.netty_proxy_sock5Common.callback;


import io.netty.channel.Channel;

public interface ConnectCallBack {
    void success(Channel channel);
    void error();
}
