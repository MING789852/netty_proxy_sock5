# netty_proxy_sock5

基于netty实现的socks5代理

 

# 使用教程

一、进入文件夹netty_proxy_sock5_common，使用下面两个命令

```
mvn clean
mvn install
```

二、修改netty_proxy_sock5_client和netty_proxy_sock5_server中的连接用户密码、端口号

三、进入文件夹netty_proxy_sock5_server，使用下面两个命令

```
mvn clean
mvn install
```

生成target文件夹后打开文件夹，里面有可执行jar包Socks5_Server.jar，把jar包放到服务器使用

```
java -jar Socks5_Server.jar
```

四、进入文件夹netty_proxy_sock5_client，使用下面两个命令

```
mvn clean
mvn install
```

生成target文件夹后打开文件夹，里面有可执行jar包Socks5_Client.jar，本地执行

```
java -jar Socks5_Client.jar
```

五、浏览器下载相关代理工具比如SwitchyOmega，配置127.0.0.1 和 client的端口
