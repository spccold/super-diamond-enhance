package com.github.diamond.web.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.github.diamond.jetty.JettyServer;
import com.github.diamond.model.MessageBody;
import com.github.diamond.netty.DiamondServerHandler;
import com.github.diamond.utils.HttpUtils;
import com.github.diamond.utils.JsonUtils;
import com.github.diamond.web.model.ClientInfoResp;
import com.github.diamond.web.model.HttpPushResponse;
import com.github.diamond.web.model.ShowPushData;

@Service
public class ShowPushService {
    /** 初始化日志 */
    private static final Logger                     LOGGER       = LoggerFactory.getLogger(ShowPushService.class);

    private static final SimpleDateFormat           DATEFORMATER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 创建执行推送的线程池 */
    private static final ExecutorService            service      = Executors.newCachedThreadPool();

    private static final Map<String, AtomicInteger> retryMap     = new HashMap<String, AtomicInteger>();

    @Autowired
    private JdbcTemplate                            jdbcTemplate;

    @Autowired
    private ProjectService                          projectService;

    @Autowired
    private DiamondServerHandler                    serverHandler;

    /**
     * 异步推送数据
     */
    public void pushGlobalData(final String clientAddress, final String moduleName, final String configKey) {
        // 想线程池提交一个推送任务
        service.submit(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("[global]push task start......");
                List<ShowPushData> pushDatas = listNeedPushClients(clientAddress);
                if (!CollectionUtils.isEmpty(pushDatas)) {
                    for (ShowPushData data : pushDatas) {
                        // 循环推送数据
                        serverHandler.pushConfigForHeartBeat(data.getClientAddress(), data.getProjectCode(),
                            data.getProfileName(), moduleName, configKey, null);
                        // 更新need_push为0
                        updateNeedPush(data.getProjectCode(), data.getProfileName(), data.getModuleNames(),
                            data.getClientAddress());
                    }
                }
                LOGGER.info("[global]push task end......");
            }
        });
    }

    /**
     * 异步推送数据
     */
    public void pushPartData(final String clientAddress, final String moduleName, final String configKey,
                             final String configValue) {
        // 想线程池提交一个推送任务
        service.submit(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("[part]push task start......");

                List<ShowPushData> pushDatas = listNeedPushClients(clientAddress);
                if (!CollectionUtils.isEmpty(pushDatas)) {
                    for (ShowPushData data : pushDatas) {
                        // 循环推送数据
                        serverHandler.pushConfigForHeartBeat(data.getClientAddress(), data.getProjectCode(),
                            data.getProfileName(), moduleName, configKey, configValue);
                    }
                }
                LOGGER.info("[part]push task end......");
            }
        });
    }

    /**
     * 连续3次,如果第四次还是探测失败，那么认定该server已经宕机
     */
    public void clearServerInfoBySchedule() {
        List<String> serverUrls = queryAllOtherServerInfos();
        AtomicInteger integer = null;
        String serverAddress = null;
        if (!CollectionUtils.isEmpty(serverUrls)) {
            LOGGER.info("需要探测的服务器信息为: " + JsonUtils.jsonFromObject(serverUrls));
            List<HttpPushResponse> responses = HttpUtils.postDetect(serverUrls);
            if (!CollectionUtils.isEmpty(responses)) {
                for (HttpPushResponse response : responses) {
                    serverAddress = response.getAddress();
                    if (response.isRet()) {//一次成功，清楚之前所有失败记录
                        LOGGER.info(serverAddress + "探测成功");
                        retryMap.remove(serverAddress);
                    } else {
                        if (retryMap.containsKey(serverAddress)) {
                            integer = retryMap.get(serverAddress);
                            if (integer.get() >= 3) {//3次连接全部失败
                                retryMap.remove(serverAddress);
                                //删除该server在SERVER_INFO中的信息
                                LOGGER.info(serverAddress + "已经宕机");
                                deleteServer(serverAddress);
                            } else {
                                //失败+1
                                int ret = integer.incrementAndGet();
                                LOGGER.info(serverAddress + "探测失败，这是第" + ret + "次");
                                retryMap.put(serverAddress, integer);
                            }
                        } else {
                            retryMap.put(serverAddress, new AtomicInteger(1));
                            LOGGER.info(serverAddress + "探测失败，这是第1次");
                        }
                    }
                }
            }
        } else {
            LOGGER.info("没发现需要探测的服务器");
        }
    }

    /**
     * 删除宕机的机器
     * 
     * @param serverAddress
     */
    @Transactional
    private void deleteServer(String serverAddress) {
        //1.根据SERVER_ADDRESS_PORT查询server_uuid
        String sql = "select SERVER_UUID from SERVER_INFO where SERVER_ADDRESS_PORT = ?";
        String serverUUID = jdbcTemplate.queryForObject(sql, String.class, serverAddress);
        if (StringUtils.isBlank(serverUUID)) {
            throw new RuntimeException("根据SERVER_ADDRESS_PORT查询SERVER_UUID失败");
        }
        //2.删除serverUUID对应的多有client信息
        sql = "delete from CONF_SHOW_PUSH where SERVER_UUID = ?";
        jdbcTemplate.update(sql, serverUUID);

        //3.删除该server在SERVER_INFO中的信息
        sql = "delete from SERVER_INFO where SERVER_ADDRESS_PORT = ?";
        jdbcTemplate.update(sql, serverAddress);
    }

    /**
     * 
     */
    public void clearClientBySchedule() {
        //XXX sql中的28秒和客户端发送心跳时间的间隔相关,请注意
        String sql = "delete from CONF_SHOW_PUSH where timestampadd(second, 28, LAST_CONN_TIME) < ?";
        try {
            int ret = jdbcTemplate.update(sql, new Date());
            LOGGER.info("清除失活客户端,ret = " + ret);
        } catch (DataAccessException e) {
            LOGGER.error("清除失活的client失败", e);
        }
    }

    /**
     * 根据客户端地址更新客户端连接时间
     * 
     * @param clientAddress
     */
    public void updateConnTimeByClientAddress(MessageBody body, Integer ctxIdentifier) {
        //首先检测当前客户端连接是否存在
        String sql = "select count(*) from CONF_SHOW_PUSH where CLIENT_ADDRESS=?";
        Long ret = null;
        try {
            ret = jdbcTemplate.queryForObject(sql, Long.class, body.getClientAddress());
        } catch (DataAccessException e) {
            LOGGER.error("查询客户端是否存在失败", e);
        }

        if (null != ret && ret >= 1) {//存在则更新
            sql = "update CONF_SHOW_PUSH set LAST_CONN_TIME=? where CLIENT_ADDRESS=?";
            try {
                jdbcTemplate.update(sql, new Date(), body.getClientAddress());
            } catch (DataAccessException e) {
                LOGGER.error("根据客户端地址更新客户端连接时间失败", e);
            }
        } else {//不存在则插入
            ShowPushData data = new ShowPushData();
            data.setProjectCode(body.getProjCode());
            data.setProfileName(body.getProfile());
            String[] moduleArr = StringUtils.split(body.getModules(), ",");
            if (ArrayUtils.isEmpty(moduleArr)) {
                data.setModuleNames("ALL");
            } else {
                data.setModuleNames(StringUtils.join(moduleArr, ","));
            }
            data.setServerUUID(DiamondServerHandler.SERVER_UUID);
            data.setClientAddress(body.getClientAddress());
            data.setCtxIdentifier(ctxIdentifier);
            data.setConnTime(new Date());
            //默认不需要推送
            data.setNeedPush("0");
            data.setClientType(body.getClientType());
            //持久化到DB
            addShowPushDate(data);
        }
    }

    /**
     * 保存推送和展示数据
     * 
     * @param data
     */
    public void addShowPushDate(ShowPushData data) {
        String sql = "insert into CONF_SHOW_PUSH values(?,?,?,?,?,?,?,?,?,?)";
        try {
            jdbcTemplate.update(sql, data.getProjectCode(), data.getProfileName(), data.getModuleNames(),
                data.getClientAddress(), data.getCtxIdentifier(), data.getConnTime(), data.getConnTime(),
                data.getServerUUID(), data.getNeedPush(), data.getClientType());
        } catch (DataAccessException e) {
            LOGGER.error("保存推送数据失败", e);
        }

    }

    /**
     * 客户端断开连接，删除该条连接信息
     * 
     * @param data
     */
    public void deleteShowPushData(ShowPushData data) {
        String sql = "delete from CONF_SHOW_PUSH where PROJ_CODE=? and PROFILE_NAME=? and MODULE_NAMES=? and CTX_IDENTIFIER=? and SERVER_UUID=?";
        try {
            jdbcTemplate.update(sql, data.getProjectCode(), data.getProfileName(), data.getModuleNames(),
                data.getCtxIdentifier(), data.getServerUUID());
        } catch (DataAccessException e) {
            LOGGER.error("删除推送数据失败", e);
        }
    }

    /**
     * 查询某个配置项下具体的客户端连接情况
     * 
     * @param data
     * @return
     */
    public List<ClientInfoResp> showClintInfos(ShowPushData data) {
        String sql = "select PROJ_CODE,PROFILE_NAME,MODULE_NAMES,CLIENT_ADDRESS,CONN_TIME,LAST_CONN_TIME,SERVER_UUID,NEED_PUSH,CLIENT_TYPE"
                     + " from CONF_SHOW_PUSH where PROJ_CODE=? and PROFILE_NAME=?";
        List<ShowPushData> pushDatas = null;
        try {
            pushDatas = jdbcTemplate.query(sql, new PushDataRowmapper(), data.getProjectCode(), data.getProfileName());
        } catch (DataAccessException e) {
            LOGGER.error("查询配置项下的客户端列表失败", e);
        }
        // 作为查询条件，这里面的moduleName只包含一个
        String moduleName = data.getModuleNames();

        List<ClientInfoResp> resps = null;
        ClientInfoResp resp = null;
        if (!CollectionUtils.isEmpty(pushDatas)) {
            resps = new ArrayList<ClientInfoResp>();
            for (ShowPushData pushData : pushDatas) {
                // 当前module是当前客户端所关注的，那么才会显示该条客户端连接信息
                if (pushData.getModuleNames().contains(moduleName) || "ALL".equals(pushData.getModuleNames())) {
                    resp = new ClientInfoResp();
                    resp.setAddress(pushData.getClientAddress().substring(1));
                    resp.setConnTime(DATEFORMATER.format(pushData.getConnTime()));
                    resp.setLastConnTime(DATEFORMATER.format(pushData.getLastConnTime()));
                    resp.setClientType(pushData.getClientType());
                    resp.setProjectName(projectService.queryProjectNameByCode(pushData.getProjectCode()));
                    resps.add(resp);
                }
            }
        }
        return resps;
    }

    /**
     * 更新CONF_SHOW_PUSH中need_push字段,自身服务器的信息省略
     */
    public void updateNeedPush(String projectCode, String profileName, String moduleNames, String clientAddress) {
        if (StringUtils.isNotBlank(clientAddress)) {
            String sql = "update CONF_SHOW_PUSH set NEED_PUSH = 0 where PROJ_CODE=? and PROFILE_NAME=? and MODULE_NAMES=? and SERVER_UUID=? and CLIENT_ADDRESS=?";
            try {
                jdbcTemplate.update(sql, projectCode, profileName, moduleNames, DiamondServerHandler.SERVER_UUID,
                    clientAddress);
            } catch (DataAccessException e) {
                LOGGER.error("修改Need_push字段为0失败", e);
            }
        } else {
            String sql = "update CONF_SHOW_PUSH set NEED_PUSH = 1 where PROJ_CODE=? and PROFILE_NAME=? and SERVER_UUID != ? and (locate(?,MODULE_NAMES) >0 or MODULE_NAMES='ALL')";
            try {
                jdbcTemplate.update(sql, projectCode, profileName, DiamondServerHandler.SERVER_UUID, moduleNames);
            } catch (DataAccessException e) {
                LOGGER.error("修改Need_push字段为1失败", e);
            }
        }
    }

    /**
     * 查询出当前服务器需要推送数据的客户端
     */
    public List<ShowPushData> listNeedPushClients(String clientAddress) {
        if (StringUtils.isBlank(clientAddress)) {// 全局推送
            String sql = "select PROJ_CODE,PROFILE_NAME,MODULE_NAMES,CLIENT_ADDRESS,CONN_TIME,LAST_CONN_TIME,SERVER_UUID,NEED_PUSH,CLIENT_TYPE from CONF_SHOW_PUSH"
                         + " where SERVER_UUID=? and NEED_PUSH = 1";
            List<ShowPushData> pushDatas = null;
            try {
                pushDatas = jdbcTemplate.query(sql, new PushDataRowmapper(), DiamondServerHandler.SERVER_UUID);
            } catch (DataAccessException e) {
                LOGGER.error("查询出当前服务器需要推送数据的客户端失败", e);
            }
            return pushDatas;
        } else {// 局部推送
            String sql = "select PROJ_CODE,PROFILE_NAME,MODULE_NAMES,CLIENT_ADDRESS,CONN_TIME,LAST_CONN_TIME,SERVER_UUID,NEED_PUSH,CLIENT_TYPE from CONF_SHOW_PUSH"
                         + " where SERVER_UUID=? and CLIENT_ADDRESS=?";
            List<ShowPushData> pushDatas = null;
            try {
                pushDatas = jdbcTemplate.query(sql, new PushDataRowmapper(), DiamondServerHandler.SERVER_UUID,
                    clientAddress);
            } catch (DataAccessException e) {
                LOGGER.error("查询出当前服务器需要推送数据的客户端失败", e);
            }
            return pushDatas;
        }
    }

    // 查询除当前的所有服务器地址以及端口
    public List<String> queryAllOtherServerInfos() {
        String sql = "select SERVER_ADDRESS_PORT from SERVER_INFO where SERVER_ADDRESS_PORT != ?";
        String address = JettyServer.serverHost + ":" + JettyServer.serverPort;
        try {
            return jdbcTemplate.queryForList(sql, String.class, address);
        } catch (DataAccessException e) {
            LOGGER.error("查询服务器信息失败", e);
            return null;
        }
    }

    /**
     * 
     * 
     * @param address
     * @return
     */
    public Integer getCurrentServerCtxIdentifierByIp(String address) {
        String sql = "select CTX_IDENTIFIER from CONF_SHOW_PUSH where CLIENT_ADDRESS= ? and SERVER_UUID=?";
        List<Integer> ctxIdentifiers = null;
        try {
            ctxIdentifiers = jdbcTemplate.queryForList(sql, Integer.class, address, DiamondServerHandler.SERVER_UUID);
        } catch (DataAccessException e) {
            LOGGER.error("查询ctxIdentifier失败!", e);
        }
        if (!CollectionUtils.isEmpty(ctxIdentifiers)) {
            return ctxIdentifiers.get(0);
        }
        return null;
    }

    public String getClientIpByCurrentCtxIdentifier(Integer ctxIdentifier) {
        String sql = "select CLIENT_ADDRESS from CONF_SHOW_PUSH where CTX_IDENTIFIER= ? and SERVER_UUID=?";
        List<String> ips = null;
        try {
            ips = jdbcTemplate.queryForList(sql, String.class, ctxIdentifier, DiamondServerHandler.SERVER_UUID);
        } catch (DataAccessException e) {
            LOGGER.error("查询clientIp失败!", e);
        }
        if (!CollectionUtils.isEmpty(ips)) {
            return ips.get(0);
        }
        return null;
    }

    public boolean clientConnected(String clientAddress) {
        String sql = "select count(*) from CONF_SHOW_PUSH where CLIENT_ADDRESS = ?";
        Long ret = null;
        try {
            ret = jdbcTemplate.queryForObject(sql, Long.class, clientAddress);
            return (ret != null && ret >= 1);
        } catch (DataAccessException e) {
            return false;
        }
    }

    private class PushDataRowmapper implements RowMapper<ShowPushData> {
        @Override
        public ShowPushData mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShowPushData data = new ShowPushData();
            data.setProjectCode(rs.getString(1));
            data.setProfileName(rs.getString(2));
            data.setModuleNames(rs.getString(3));
            data.setClientAddress(rs.getString(4));
            data.setConnTime(rs.getTimestamp(5));
            data.setLastConnTime(rs.getTimestamp(6));
            data.setServerUUID(rs.getString(7));
            data.setNeedPush(rs.getString(8));
            data.setClientType(rs.getString(9));
            return data;
        }

    }

}
