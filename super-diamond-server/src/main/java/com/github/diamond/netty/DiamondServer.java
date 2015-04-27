/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.diamond.utils.NamedThreadFactory;
import com.github.diamond.web.service.ShowPushService;

/**
 * Create on @2013-8-24 @上午10:03:59 
 * @author bsli@ustcinfo.com
 */
public class DiamondServer implements InitializingBean, DisposableBean {

    private static final Logger                      LOGGER                    = LoggerFactory
                                                                                   .getLogger(DiamondServer.class);

    //客户端检查间隔   单位:毫秒
    private static final int                         CHECK_CLIENT_INTERVAL     = 30 * 1000;

    //客户端检查间隔   单位:毫秒
    private static final int                         CHECK_SERVER_INTERVAL     = 15 * 1000;

    private EventLoopGroup                           bossGroup                 = new NioEventLoopGroup(1);

    private EventLoopGroup                           workerGroup               = new NioEventLoopGroup();

    private DiamondServerHandler                     serverHandler;

    //客户端
    private ScheduledFuture<?>                       clearClientExecutorFuture = null;
    //服务端
    private ScheduledFuture<?>                       clearServerExecutorFuture = null;

    private static final ScheduledThreadPoolExecutor clearExecutorService      = new ScheduledThreadPoolExecutor(2,
                                                                                   new NamedThreadFactory(
                                                                                       "clearClientAndServiceInfo",
                                                                                       true));
    @Autowired
    private ShowPushService                          pushService;

    @Override
    public void afterPropertiesSet() throws Exception {
        //default value
        String nettyHost = "0.0.0.0";
        int nettyPort = 8283;
        try {
            org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(
                "META-INF/res/netty.properties");

            LOGGER.info("加载netty.properties");

            nettyHost = config.getString("netty.host");
            nettyPort = config.getInt("netty.port");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
            .option(ChannelOption.SO_REUSEADDR, true).childHandler(new DiamondServerInitializer(serverHandler));

        b.bind(nettyHost, nettyPort).sync().channel();
        LOGGER.info("启动 Diamond Netty Server, post={}", nettyPort);

        Runnable clearClientCheckCommand = new Runnable() {
            public void run() {
                //检查哪些client已经失活
                pushService.clearClientBySchedule();
            }
        };

        Runnable clearServerCheckCommand = new Runnable() {
            public void run() {
                //检查哪些server已经异常宕机
                pushService.clearServerInfoBySchedule();
            }
        };

        clearClientExecutorFuture = clearExecutorService.scheduleWithFixedDelay(clearClientCheckCommand,
            CHECK_CLIENT_INTERVAL, CHECK_CLIENT_INTERVAL, TimeUnit.MILLISECONDS);

        clearServerExecutorFuture = clearExecutorService.scheduleWithFixedDelay(clearServerCheckCommand,
            CHECK_SERVER_INTERVAL, CHECK_SERVER_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void destroy() throws Exception {
        if (bossGroup != null)
            bossGroup.shutdownGracefully();

        if (workerGroup != null)
            workerGroup.shutdownGracefully();

        //清除线程池
        destroyConnectStatusCheckCommand();
    }

    public DiamondServerHandler getServerHandler() {
        return serverHandler;
    }

    public void setServerHandler(DiamondServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    private synchronized void destroyConnectStatusCheckCommand() {
        try {
            if (clearClientExecutorFuture != null && !clearClientExecutorFuture.isDone()) {
                clearClientExecutorFuture.cancel(true);
                clearExecutorService.purge();
                clearClientExecutorFuture = null;
            }
            if (clearServerExecutorFuture != null && !clearServerExecutorFuture.isDone()) {
                clearServerExecutorFuture.cancel(true);
                clearExecutorService.purge();
                clearServerExecutorFuture = null;
            }
        } catch (Throwable e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
