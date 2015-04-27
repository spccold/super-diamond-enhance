package com.github.diamond.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.github.diamond.web.model.ClientInfoResp;
import com.github.diamond.web.model.HttpPushResponse;
import com.github.diamond.web.model.ShowPushData;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ProjectService;
import com.github.diamond.web.service.ShowPushService;

@Controller
public class APIController extends BaseController {
    private static final Logger  LOGGER = LoggerFactory.getLogger(APIController.class);

    @Autowired
    private ProjectService       projectService;
    @Autowired
    private ShowPushService      pushService;
    @Autowired
    private ConfigService        configService;
    @Autowired
    private DiamondServerHandler diamondServerHandler;

    /**
     * 根据drm标识获取当前资源的客户端列表
     * @param projectCode
     * @param prfileName
     * @param moduleName
     * @param response
     */
    @RequestMapping("/api/{projectCode}/{profileName}/{moduleName}")
    public void getConfigClientInfos(@PathVariable("projectCode") String projectCode,
                                     @PathVariable("profileName") String profileName,
                                     @PathVariable("moduleName") String moduleName, HttpServletResponse response) {
        LOGGER.info("[API] query client[projectCode: " + projectCode + ", profileName: " + profileName
                    + ", moduleName: " + moduleName + "] infos start");

        ShowPushData data = new ShowPushData();
        data.setProjectCode(projectCode);
        data.setProfileName(profileName);
        data.setModuleNames(moduleName);
        List<ClientInfoResp> resps = pushService.showClintInfos(data);
        // 查询结果转化成json串返回客户端，用于展示
        String content = JsonUtils.jsonFromObject(resps);
        LOGGER.info("[API] the client infos detail is: " + content);
        this.outputContent(response, content);
    }

    @RequestMapping("/api/partPush/{projectCode}/{profileName}/{moduleName}")
    public void pushPartConfig(@PathVariable("projectCode") String projectCode,
                               @PathVariable("profileName") String profileName,
                               @PathVariable("moduleName") String moduleName, HttpServletRequest request,
                               HttpServletResponse response) {
        LOGGER.info("[API][part]push start......");
        String clientAddress = request.getParameter("clientAddress");
        String configValue = request.getParameter("configValue");
        String configKey = request.getParameter("configKey");

        LOGGER.info("[API][part]push itself clients start......");
        // 2.对自己长连的客户端进行推送
        // 局部推送 每次只会对一个客户端进行推送，如果恰好这个客户单是当前server长连的客户单，那就不需要通知其它server进行推送了
        boolean pushed = diamondServerHandler.pushConfigManual(projectCode, profileName, moduleName, configKey,
            configValue, "/" + clientAddress);
        LOGGER.info("[API][part]push itself clients end......");
        LOGGER.info("[API][part]query other server address start......");

        String errorMsg = "SUCCESS";
        if (!pushed) {// 还没进行推送
            // 3.触发其他服务器进行推送
            List<String> addresses = pushService.queryAllOtherServerInfos();
            LOGGER.info("[API][part]the server address list is: " + JsonUtils.jsonFromObject(addresses));

            List<HttpPushResponse> responses = HttpUtils.post(clientAddress, moduleName, configKey, configValue,
                addresses);
            LOGGER.info("[API][part]the push responses: " + JsonUtils.jsonFromObject(responses));
            LOGGER.info("[API][part]push end......");
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
    @RequestMapping("/api/globalPush/{projectCode}/{profileName}/{moduleName}")
    public void pushGlobalConfig(@PathVariable("projectCode") String projectCode,
                                 @PathVariable("profileName") String profileName,
                                 @PathVariable("moduleName") String moduleName, HttpServletRequest request,
                                 HttpServletResponse response) {
        LOGGER.info("[API][global]push start......");
        //根据projectId查询出projectCode
        String configKey = request.getParameter("configKey");
        //1.修改CONF_SHOW_PUSH 的need_push为1 
        pushService.updateNeedPush(projectCode, profileName, moduleName, null);
        LOGGER.info("[API][global]push itself clients start......");
        //2.对自己长连的客户端进行推送
        diamondServerHandler.pushConfigManual(projectCode, profileName, moduleName, configKey, null, null);
        LOGGER.info("[API][global]push itself clients end......");
        LOGGER.info("[API][global]query other server address start......");
        //3.触发其他服务器进行推送
        List<String> addresses = pushService.queryAllOtherServerInfos();
        LOGGER.info("[API][global]the server address list is: " + JsonUtils.jsonFromObject(addresses));

        List<HttpPushResponse> responses = HttpUtils.post(null, moduleName, configKey, null, addresses);
        LOGGER.info("[API][global]the push responses: " + JsonUtils.jsonFromObject(responses));
        LOGGER.info("[API][global]push end......");

        String errorMsg = "SUCCESS";
        if (!CollectionUtils.isEmpty(responses)) {
            errorMsg = "触发其它服务器推送配置失败,具体失败的服务器的信息如下: " + JsonUtils.jsonFromObject(responses);
        }
        this.outputContent(response, errorMsg);
    }

    @RequestMapping("/api/updateConfig/{projectCode}/{profileName}")
    public void updateConfigValue(@PathVariable("projectCode") String projectCode,
                                  @PathVariable("profileName") String profileName, HttpServletRequest request,
                                  HttpServletResponse response) {
        String configKey = request.getParameter("configKey");
        String configValue = request.getParameter("configValue");
        LOGGER.info("[API] update config[projectCode: " + projectCode + ", profileName: " + profileName
                    + ", configKey: " + configKey + "]" + " value start");
        Long projectId = projectService.queryProjectIdByCode(projectCode);
        String retMsg = "SUCCESS";
        try {
            configService.updateConfigByApi(projectId, profileName, configKey, configValue);
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            retMsg = "SUCCESS";
        }
        LOGGER.info("[API] update config[projectCode: " + projectCode + ", profileName: " + profileName
                    + ", configKey: " + configKey + "]" + " value end");
        this.outputContent(response, retMsg);
    }
}
