/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.diamond.netty.DiamondServerHandler;
import com.github.diamond.utils.HttpUtils;
import com.github.diamond.utils.JsonUtils;
import com.github.diamond.web.model.HttpPushResponse;
import com.github.diamond.web.service.ProjectService;
import com.github.diamond.web.service.ShowPushService;

/**
 * Create on @2013-8-23 @上午11:46:19 
 * @author bsli@ustcinfo.com
 */
@Controller
public class PushController extends BaseController {

    private static final Logger  LOGGER = LoggerFactory.getLogger(PushController.class);

    @Autowired
    private ShowPushService      pushService;

    @Autowired
    private ProjectService       projectService;

    @Autowired
    private DiamondServerHandler diamondServerHandler;

    @RequestMapping("/partPush/{projectId}/{profileName}/{moduleName}")
    public void pushPartConfig(@PathVariable("projectId") Long projectId,
                               @PathVariable("profileName") String profileName,
                               @PathVariable("moduleName") String moduleName, HttpServletRequest request,
                               HttpServletResponse response) {
        LOGGER.info("[part]push start......");
        String clientAddress = request.getParameter("clientAddress");
        String configValue = request.getParameter("configValue");
        String configKey = request.getParameter("configKey");
        //根据projectId查询出projectCode
        String projectCode = projectService.queryProjectCodeById(projectId);

        LOGGER.info("[part]push itself clients start......");
        //2.对自己长连的客户端进行推送
        //局部推送  每次只会对一个客户端进行推送，如果恰好这个客户单是当前server长连的客户单，那就不需要通知其它server进行推送了
        boolean pushed = diamondServerHandler.pushConfigManual(projectCode, profileName, moduleName, configKey,
            configValue, "/" + clientAddress);
        LOGGER.info("[part]push itself clients end,the result: " + pushed);
        LOGGER.info("[part]query other server address start......");

        String errorMsg = StringUtils.EMPTY;
        if (!pushed) {//还没进行推送
            //3.触发其他服务器进行推送
            List<String> addresses = pushService.queryAllOtherServerInfos();
            LOGGER.info("[part]the server address list is: " + JsonUtils.jsonFromObject(addresses));

            List<HttpPushResponse> responses = HttpUtils.post(clientAddress, moduleName, configKey, configValue,
                addresses);
            LOGGER.info("[part]the push responses: " + JsonUtils.jsonFromObject(responses));
            LOGGER.info("[part]push end......");
            if (!CollectionUtils.isEmpty(responses)) {
                errorMsg = "触发其它服务器推送配置失败,具体失败的服务器的信息如下: " + JsonUtils.jsonFromObject(responses);
            }
        }
        this.outputContent(response, errorMsg);
    }

    /**
     * 全局推送
     * 对订阅当前key的客户端做全部推送
     * 
     * @param projectId
     * @param profileName
     * @param moduleName
     * @param configKey
     * @param request
     * @param response
     */
    @RequestMapping("/globalPush/{projectId}/{profileName}/{moduleName}")
    public void pushGlobalConfig(@PathVariable("projectId") Long projectId,
                                 @PathVariable("profileName") String profileName,
                                 @PathVariable("moduleName") String moduleName, HttpServletRequest request,
                                 HttpServletResponse response) {
        LOGGER.info("[global]push start......");
        //根据projectId查询出projectCode
        String projectCode = projectService.queryProjectCodeById(projectId);
        String configKey = request.getParameter("configKey");
        //1.修改CONF_SHOW_PUSH 的need_push为1 
        pushService.updateNeedPush(projectCode, profileName, moduleName, null);
        LOGGER.info("[global]push itself clients start......");
        //2.对自己长连的客户端进行推送
        diamondServerHandler.pushConfigManual(projectCode, profileName, moduleName, configKey, null, null);
        LOGGER.info("[global]push itself clients end......");
        LOGGER.info("[global]query other server address start......");
        //3.触发其他服务器进行推送
        List<String> addresses = pushService.queryAllOtherServerInfos();
        LOGGER.info("[global]the server address list is: " + JsonUtils.jsonFromObject(addresses));

        List<HttpPushResponse> responses = HttpUtils.post(null, moduleName, configKey, null, addresses);
        LOGGER.info("[global]the push responses: " + JsonUtils.jsonFromObject(responses));
        LOGGER.info("[global]push end......");

        String errorMsg = StringUtils.EMPTY;
        if (!CollectionUtils.isEmpty(responses)) {
            errorMsg = "触发其它服务器推送配置失败,具体失败的服务器的信息如下: " + JsonUtils.jsonFromObject(responses);
        }
        this.outputContent(response, errorMsg);
    }
}
