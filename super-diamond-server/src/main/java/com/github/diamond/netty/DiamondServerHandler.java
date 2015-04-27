/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.support.json.JSONUtils;
import com.github.diamond.model.MessageBody;
import com.github.diamond.utils.JsonUtils;
import com.github.diamond.web.model.ShowPushData;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ShowPushService;

/**
 * Create on @2013-8-24 @上午10:05:25 
 * @author bsli@ustcinfo.com
 */
@Sharable
public class DiamondServerHandler extends SimpleChannelInboundHandler<String> {
    /**生成唯一标识该台服务器的UUID*/
    public static final String                                   SERVER_UUID = UUID.randomUUID().toString()
                                                                                 .toLowerCase().replace("-", "");

    public static ConcurrentHashMap<ClientKey, List<ClientInfo>> clients     = new ConcurrentHashMap<ClientKey, List<ClientInfo>>();

    private ConcurrentHashMap<Integer, ChannelHandlerContext>    channels    = new ConcurrentHashMap<Integer, ChannelHandlerContext>();
    private static final Logger                                  LOGGER      = LoggerFactory
                                                                                 .getLogger(DiamondServerHandler.class);

    private final Charset                                        charset     = Charset.forName("UTF-8");

    @Autowired
    private ConfigService                                        configService;

    @Autowired
    private ShowPushService                                      showPushService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info(ctx.channel().remoteAddress() + " 连接到服务器。");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        //获取netty上下文在内存中的位置，也就是唯一标识
        Integer ctxIdentifier = System.identityHashCode(ctx);

        //判断消息类型(配置请求/心跳)
        if (StringUtils.isNotBlank(request) && request.startsWith("heartbeat")) {//心跳请求
            String messageBody = request.substring("heartbeat=".length());
            MessageBody body = JsonUtils.objectFromJson(messageBody, MessageBody.class);
            LOGGER.debug("收到client:" + body.getClientAddress() + "的心跳");
            body.setClientType("JAVA");
            showPushService.updateConnTimeByClientAddress(body, ctxIdentifier);
        } else {
            String config;
            if (StringUtils.isNotBlank(request) && request.startsWith("superdiamond=")) {
                request = request.substring("superdiamond=".length());

                Map<String, String> params = (Map<String, String>) JSONUtils.parse(request);
                String clientAddress = params.get("clientAddress");
                //FIXME  防止Ha Proxy同时向多个服务器转发tcp连接请求
                //如果clientAddress已经存在，则断开该连接
                if (showPushService.clientConnected(clientAddress)) {
                    //断开连接
                    ctx.close();
                    return;
                }
                String projCode = params.get("projCode");
                String modules = params.get("modules");
                String[] moduleArr = StringUtils.split(modules, ",");
                String profile = params.get("profile");
                ClientKey key = new ClientKey();
                key.setProjCode(projCode);
                key.setProfile(profile);
                key.setModuleArr(moduleArr);
                //String version = params.get("version");

                List<ClientInfo> addrs = clients.get(key);
                if (addrs == null) {
                    addrs = new ArrayList<ClientInfo>();
                }

                Date connTime = new Date();
                ClientInfo clientInfo = new ClientInfo(ctxIdentifier, connTime);
                addrs.add(clientInfo);
                clients.put(key, addrs);
                channels.put(ctxIdentifier, ctx);

                if (StringUtils.isNotBlank(modules)) {
                    config = configService.queryConfigs(projCode, moduleArr, profile, "");
                } else {
                    config = configService.queryConfigs(projCode, profile, "");
                }
                //XXX add by kanguangwen
                //把当前连接信息存入数据库,以支持server端的分布式
                ShowPushData data = new ShowPushData();
                data.setProjectCode(projCode);
                data.setProfileName(profile);
                if (ArrayUtils.isEmpty(moduleArr)) {
                    data.setModuleNames("ALL");
                } else {
                    data.setModuleNames(StringUtils.join(moduleArr, ","));
                }
                data.setServerUUID(SERVER_UUID);
                data.setClientAddress(clientAddress);
                data.setCtxIdentifier(ctxIdentifier);
                data.setConnTime(connTime);
                //默认不需要推送
                data.setNeedPush("0");
                data.setClientType("JAVA");
                //持久化到DB
                showPushService.addShowPushDate(data);
            } else {
                config = "";
            }

            sendMessage(ctx, config);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Integer ctxIdentifier = System.identityHashCode(ctx);
        channels.remove(ctxIdentifier);

        //old code
        /*for(List<ClientInfo> infos : clients.values()) {
            for(ClientInfo client : infos) {
                if(address.equals(client.getAddress())) {
                    infos.remove(client);
                    break;
                }
            }
        }*/

        //edit by kanguangwen
        ClientKey key = null;
        List<ClientInfo> values = null;
        for (Entry<ClientKey, List<ClientInfo>> entry : clients.entrySet()) {
            key = entry.getKey();
            values = entry.getValue();
            if (!CollectionUtils.isEmpty(values)) {
                for (ClientInfo client : values) {
                    if (ctxIdentifier.equals(client.getCtxIdentifier())) {
                        values.remove(client);
                        break;
                    }
                }
            }

        }

        //XXX add by kanguangwen
        //断开连接之后,清除该条数据在数据库中的记录
        ShowPushData data = new ShowPushData();
        data.setServerUUID(SERVER_UUID);
        data.setCtxIdentifier(ctxIdentifier);
        if (null != key) {
            data.setProjectCode(key.getProjCode());
            data.setProfileName(key.getProfile());
            if (ArrayUtils.isEmpty(key.getModuleArr())) {
                data.setModuleNames("ALL");
            } else {
                data.setModuleNames(StringUtils.join(key.getModuleArr(), ","));
            }
        }
        //删除数据
        showPushService.deleteShowPushData(data);
        LOGGER.info(ctx.channel().remoteAddress() + " 断开连接。");
    }

    /**
     * 向服务端推送配置数据。
     * 
     * @param projCode
     * @param profile
     * @param config
     */
    /* public void pushConfig(String projCode, String profile, final String module) {
         for (ClientKey key : clients.keySet()) {
             if (key.getProjCode().equals(projCode) && key.getProfile().equals(profile)) {
                 List<ClientInfo> addrs = clients.get(key);
                 if (addrs != null) {
                     for (ClientInfo client : addrs) {
                         ChannelHandlerContext ctx = channels.get(client.getAddress());
                         if (ctx != null) {
                             if (key.moduleArr.length == 0) {
                                 //客户单读取配置时没有指定moduleName,当服务端修改某个项目下的某个某块下的配置时，服务端会把所有的配置重新发给客户端
                                 String config = configService.queryConfigs(projCode, profile, "");
                                 sendMessage(ctx, config);
                             } else if (ArrayUtils.contains(key.getModuleArr(), module)) {
                                 //客户单读取配置时指定moduleName时(可以是多个,用逗号分隔),当服务端修改某个项目下的某个某块下的配置时，服务端会把客户端指定的所有module下的所有的配置重新发给客户端
                                 String config = configService.queryConfigs(projCode, key.getModuleArr(), profile, "");
                                 sendMessage(ctx, config);
                             }
                         }
                     }
                 }
             }
         }
     }*/

    /**
     * 手动向服务端推送配置数据。
     * 
     * @param projCode
     * @param profile
     * @param config
     */
    public boolean pushConfigManual(String projCode, String profile, final String module, String configKey,
                                    String configValue, String clientAddress) {
        boolean pushed = false;
        boolean globalPush = StringUtils.isBlank(clientAddress);
        //长连客户端唯一标识
        Integer ctxIdentifier = showPushService.getCurrentServerCtxIdentifierByIp(clientAddress);
        for (ClientKey key : clients.keySet()) {
            if (key.getProjCode().equals(projCode) && key.getProfile().equals(profile)) {
                List<ClientInfo> addrs = clients.get(key);
                if (addrs != null) {
                    for (ClientInfo client : addrs) {
                        if (StringUtils.isBlank(configValue) || client.getCtxIdentifier().equals(ctxIdentifier)) {//匹配指定的客户端进行推送

                            ChannelHandlerContext ctx = channels.get(client.getCtxIdentifier());
                            if (ctx != null) {
                                //把单条记录数据推送到关注过这个配置的项目去
                                if (key.moduleArr.length == 0 || ArrayUtils.contains(key.getModuleArr(), module)) {
                                    String config = configService.queryConfigsForMannul(projCode, profile, module,
                                        configKey, configValue);
                                    sendMessage(ctx, config);
                                    pushed = true;

                                    //全局推送的情况，需要到数据库中查询客户端ip
                                    if (globalPush) {
                                        clientAddress = showPushService.getClientIpByCurrentCtxIdentifier(client
                                            .getCtxIdentifier());
                                    }

                                    LOGGER.info("push data to slef client[projectCode: " + projCode + ",profile: "
                                                + profile + ",module: " + module + ",clientAddress: " + clientAddress
                                                + "]");
                                }
                            }

                        }
                    }
                }
            }
        }
        return pushed;
    }

    /**
     * 手动向服务端推送配置数据。
     * 
     * @param projCode
     * @param profile
     * @param config
     */
    public void pushConfigForHeartBeat(String clientAddress, String projCode, String profile, final String module,
                                       String configKey, String configValue) {
        //长连客户端唯一标识
        Integer ctxIdentifier = showPushService.getCurrentServerCtxIdentifierByIp(clientAddress);
        ChannelHandlerContext context = channels.get(ctxIdentifier);
        if (null != context) {
            String config = configService.queryConfigsForMannul(projCode, profile, module, configKey, configValue);
            sendMessage(context, config);
            LOGGER.info("push data by heartbeat[projectCode: " + projCode + ",profile: " + profile + ",module: "
                        + module + ",clientAddress: " + clientAddress + "]");
        }
    }

    private void sendMessage(ChannelHandlerContext ctx, String config) {
        byte[] bytes = config.getBytes(charset);
        ByteBuf message = Unpooled.buffer(4 + bytes.length);
        message.writeInt(bytes.length);
        message.writeBytes(bytes);
        ctx.writeAndFlush(message);
    }

    public static class ClientKey {
        String   projCode;
        String[] moduleArr;
        String   profile;

        public String getProjCode() {
            return projCode;
        }

        public void setProjCode(String projCode) {
            this.projCode = projCode;
        }

        public String[] getModuleArr() {
            return moduleArr;
        }

        public void setModuleArr(String[] moduleArr) {
            this.moduleArr = moduleArr;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(moduleArr);
            result = prime * result + ((profile == null) ? 0 : profile.hashCode());
            result = prime * result + ((projCode == null) ? 0 : projCode.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ClientKey other = (ClientKey) obj;
            if (!Arrays.equals(moduleArr, other.moduleArr))
                return false;
            if (profile == null) {
                if (other.profile != null)
                    return false;
            } else if (!profile.equals(other.profile))
                return false;
            if (projCode == null) {
                if (other.projCode != null)
                    return false;
            } else if (!projCode.equals(other.projCode))
                return false;
            return true;
        }
    }

    public static class ClientInfo {
        private Integer ctxIdentifier;
        private Date    connectTime;

        public ClientInfo(Integer ctxIdentifier, Date connectTime) {
            this.ctxIdentifier = ctxIdentifier;
            this.connectTime = connectTime;
        }

        public Integer getCtxIdentifier() {
            return ctxIdentifier;
        }

        public void setCtxIdentifier(Integer ctxIdentifier) {
            this.ctxIdentifier = ctxIdentifier;
        }

        public Date getConnectTime() {
            return connectTime;
        }

        public void setConnectTime(Date connectTime) {
            this.connectTime = connectTime;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((connectTime == null) ? 0 : connectTime.hashCode());
            result = prime * result + ((ctxIdentifier == null) ? 0 : ctxIdentifier.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ClientInfo other = (ClientInfo) obj;
            if (connectTime == null) {
                if (other.connectTime != null)
                    return false;
            } else if (!connectTime.equals(other.connectTime))
                return false;
            if (ctxIdentifier == null) {
                if (other.ctxIdentifier != null)
                    return false;
            } else if (!ctxIdentifier.equals(other.ctxIdentifier))
                return false;
            return true;
        }
    }
}