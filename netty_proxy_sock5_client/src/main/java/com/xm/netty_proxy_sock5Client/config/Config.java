package com.xm.netty_proxy_sock5Client.config;

import java.util.ResourceBundle;

public class Config {
    public static final String username;
    public static final String password;

    public static final String serverHost;
    public static final int serverPort;

    public static final int clientPort;

    public static final boolean clientOpenPool;


    static {
        ResourceBundle bundle = ResourceBundle.getBundle("application");
        username = bundle.getString("username");
        password= bundle.getString("password");
        serverHost= bundle.getString("server.host");
        serverPort= Integer.valueOf(bundle.getString("server.port"));
        clientPort=Integer.valueOf(bundle.getString("client.port"));
        clientOpenPool=Boolean.valueOf(bundle.getString("client.openPool"));
    }
}
