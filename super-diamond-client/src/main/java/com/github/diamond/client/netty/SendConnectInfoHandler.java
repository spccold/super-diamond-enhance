package com.github.diamond.client.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.diamond.client.model.MessageBody;
import com.github.diamond.client.util.JsonUtils;
import com.github.diamond.client.util.NamedThreadFactory;

/**
 * 客户端连接到server，发送client信息：superdiamond,projCode,profile\r\n
 * 
 * Create on @2013-8-24 @下午10:31:29 
 * @author bsli@ustcinfo.com
 */
public class SendConnectInfoHandler extends ChannelInboundHandlerAdapter {

    private static final Logger                      LOGGER                   = LoggerFactory
                                                                                  .getLogger(SendConnectInfoHandler.class);

    //心跳间隔       单位:毫秒
    private static final int                         HEARTBEAT_INTERVAL       = 5 * 1000;

    private MessageBody                              body;

    private final Charset                            charset;

    private ChannelHandlerContext                    channelHandlerContext    = null;

    //由于SendConnectInfoHandler为非@Sharable,所以客户端再断开重连的时候每次都会重新创建SendConnectInfoHandler
    //导致destroyConnectStatusCheckCommand()都不会被运行,把reconnectExecutorFuture置为static,destroyConnectStatusCheckCommand()
    //才会关闭不需要的心跳任务
    private static ScheduledFuture<?>                keepAliveExecutorFuture  = null;

    private static final ScheduledThreadPoolExecutor keepAliveExecutorService = new ScheduledThreadPoolExecutor(1,
                                                                                  new NamedThreadFactory("keepAlive",
                                                                                      true));

    public SendConnectInfoHandler(MessageBody body) {
        charset = Charset.forName("UTF-8");
        this.body = body;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channelHandlerContext = ctx;
        body.setClientAddress(ctx.channel().localAddress().toString());
        String msg = "superdiamond=" + JsonUtils.jsonFromObject(body) + "\r\n";
        ByteBuf encoded = Unpooled.copiedBuffer(msg, charset);
        ctx.channel().writeAndFlush(encoded);

        destroyConnectStatusCheckCommand();
        initConnectStatusCheckCommand();
    }

    private synchronized void initConnectStatusCheckCommand() {
        if (keepAliveExecutorFuture == null || keepAliveExecutorFuture.isCancelled()) {
            Runnable keepAliveCheckCommand = new Runnable() {
                public void run() {
                    //发送心跳
                    if (null != channelHandlerContext) {
                        String msg = "heartbeat=" + JsonUtils.jsonFromObject(body) + "\r\n";
                        ByteBuf encoded = Unpooled.copiedBuffer(msg, charset);
                        //LOGGER.debug("向super-diamond发起一次心跳,the ChannelHandlerContext is: " + channelHandlerContext);
                        channelHandlerContext.channel().writeAndFlush(encoded);
                    }
                }
            };
            keepAliveExecutorFuture = keepAliveExecutorService.scheduleWithFixedDelay(keepAliveCheckCommand,
                HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void destroyConnectStatusCheckCommand() {
        try {
            if (keepAliveExecutorFuture != null && !keepAliveExecutorFuture.isDone()) {
                keepAliveExecutorFuture.cancel(true);
                keepAliveExecutorService.purge();
                keepAliveExecutorFuture = null;
            }
        } catch (Throwable e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
