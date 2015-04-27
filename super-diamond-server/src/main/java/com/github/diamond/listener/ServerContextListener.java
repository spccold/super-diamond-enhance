package com.github.diamond.listener;

import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.github.diamond.jetty.JettyServer;
import com.github.diamond.netty.DiamondServerHandler;

/**
 * 自定义listener 负责当前jetty服务器启动时该服务器地址的入库操作，以及jetty关闭时，该server地址和对应客户端信息的清楚
 * 
 * @author kanguangwen
 * @version $Id: ServerContextListener.java, v 0.1 2015年2月11日 下午7:30:14 kanguangwen Exp $
 */
public class ServerContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerContextListener.class);

    private JdbcTemplate        template;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        LOGGER.info("release current server info in db start......");
        //清空该server的地址信息
        clearCurrentServerInfo(template);
        LOGGER.info("release current server info in db end......");

        //清空该server下所有的客户端连接信息
        LOGGER.info("release current server all clients start......");
        clearClientInfos(template);
        LOGGER.info("release current server all clients end......");
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        this.template = context.getBean(JdbcTemplate.class);
        dealServerInfo(template);
    }

    /**
     * 服务器启动时，记录该服务器地址信息
     * 
     * @param jdbcTemplate
     */
    private void dealServerInfo(JdbcTemplate jdbcTemplate) {
        String serverAddress = JettyServer.serverHost + ":" + JettyServer.serverPort;

        //1.根据SERVER_ADDRESS_PORT查询server_uuid
        String sql = "select SERVER_UUID from SERVER_INFO where SERVER_ADDRESS_PORT = ?";
        String serverUUID = null;
        try {
            serverUUID = jdbcTemplate.queryForObject(sql, String.class, serverAddress);
        } catch (DataAccessException e) {
            //do nothing
        }
        if (StringUtils.isNotBlank(serverUUID)) {
            //2.删除serverUUID对应的多有client信息
            sql = "delete from CONF_SHOW_PUSH where SERVER_UUID = ?";
            jdbcTemplate.update(sql, serverUUID);
        }

        //3.删除该server在SERVER_INFO中的信息
        sql = "delete from SERVER_INFO where SERVER_ADDRESS_PORT = ?";
        jdbcTemplate.update(sql, serverAddress);

        //首先检测地址和端口是否存在
        sql = "insert into SERVER_INFO values(?,?,?)";
        try {
            jdbcTemplate.update(sql, serverAddress, new Date(), DiamondServerHandler.SERVER_UUID);
        } catch (DataAccessException e) {
            LOGGER.error("清除server数据失败", e);
        }
    }

    /**
     * 服务器意外宕机，通过jvm钩子清空该server下所有长链的客户端信息
     */
    private static void clearClientInfos(JdbcTemplate jdbcTemplate) {
        String sql = "delete from CONF_SHOW_PUSH where SERVER_UUID=?";
        try {
            jdbcTemplate.update(sql, DiamondServerHandler.SERVER_UUID);
        } catch (DataAccessException e) {
            LOGGER.error("服务器宕机了，删除所有的客户端连接信息", e);
        }
    }

    private static void clearCurrentServerInfo(JdbcTemplate jdbcTemplate) {
        //清除SERVER_INFO中以当前地址和端口存在信息
        String sql = "delete from SERVER_INFO where SERVER_ADDRESS_PORT = ?";
        String address = JettyServer.serverHost + ":" + JettyServer.serverPort;
        try {
            jdbcTemplate.update(sql, address);
        } catch (DataAccessException e) {
            LOGGER.error("清楚server数据失败", e);
        }
    }
}
