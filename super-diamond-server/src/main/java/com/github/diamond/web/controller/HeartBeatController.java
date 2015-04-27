package com.github.diamond.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.diamond.web.service.ShowPushService;

/**
 * 
 * 用于接受其它服务器的心跳通知
 * 
 * @author kanguangwen
 * @version $Id: HeartBeatController.java, v 0.1 2015年2月4日 下午8:10:17 kanguangwen Exp $
 */
@Controller
public class HeartBeatController extends BaseController {
    private static final Logger LOGGER         = LoggerFactory.getLogger(HeartBeatController.class);

    private static final String CLIENT_ADDRESS = "clientAddres";
    private static final String MODULE_NAME    = "moduleName";
    private static final String CONFIG_KEY     = "configKey";
    private static final String CONFIG_VALUE   = "configValue";

    @Autowired
    private ShowPushService     pushService;

    /**
     * 接收推送消息
     */
    @RequestMapping("heartbeat")
    public void receiveHeartBeat(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("receiveHeartBeat from " + request.getRemoteAddr());
        final String moduleName = request.getParameter(MODULE_NAME);
        final String configKey = request.getParameter(CONFIG_KEY);
        final String clientAddres = "/" + request.getParameter(CLIENT_ADDRESS);
        final String configValue = request.getParameter(CONFIG_VALUE);

        if (StringUtils.isNotBlank(configKey)) {
            if (StringUtils.isBlank(configValue)) {//全局推送
                pushService.pushGlobalData(null, moduleName, configKey);
            } else {//局部推送
                pushService.pushPartData(clientAddres, moduleName, configKey, configValue);
            }
            String json = "{\"msg\":\"success\"}";
            this.outputContent(response, json);
        } else {
            String json = "{\"msg\":\"fail\"}";
            this.outputContent(response, json);
        }
    }

    /**
     * 接收推送消息
     */
    @RequestMapping("aliveDetect")
    public void receiveDetect(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("aliveDetect from " + request.getRemoteAddr());
        String json = "{\"msg\":\"success\"}";
        //发送存活响应
        this.outputContent(response, json);
    }
}
