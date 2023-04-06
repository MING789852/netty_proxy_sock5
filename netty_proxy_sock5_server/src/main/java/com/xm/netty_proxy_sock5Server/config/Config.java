package com.xm.netty_proxy_sock5Server.config;

import java.util.ResourceBundle;

public class Config {
    public static final String username;
    public static final String password;

    public static final int serverPort;


    static {
        ResourceBundle bundle = ResourceBundle.getBundle("application");
        username = bundle.getString("username");
        password= bundle.getString("password");
        serverPort= Integer.valueOf(bundle.getString("server.port"));
    }
}
