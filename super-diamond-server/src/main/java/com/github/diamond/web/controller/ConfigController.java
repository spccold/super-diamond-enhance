/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.diamond.model.MessageBody;
import com.github.diamond.netty.DiamondServerHandler;
import com.github.diamond.utils.OpenValidator;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.ShowPushData;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ModuleService;
import com.github.diamond.web.service.ProjectService;
import com.github.diamond.web.service.ShowPushService;

/**
 * Create on @2013-8-23 @上午11:46:19 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ConfigController extends BaseController {
    private static final Logger  LOGGER = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private ConfigService        configService;
    @Autowired
    private ProjectService       projectService;
    @Autowired
    private ModuleService        moduleService;
    @Autowired
    private DiamondServerHandler diamondServerHandler;
    @Autowired
    private ShowPushService showPushService;

    /**
     * 
     * @param type profile的值
     * @param configId
     * @param configKey
     * @param configValue
     * @param configDesc
     * @param projectId
     * @param moduleId
     * @param selModuleId
     * @param page
     * @param flag
     * @return
     */
    @RequestMapping("/config/save")
    public String saveConfig(String type, Long configId, String configKey, String configValue, String configDesc,
                             String configType, String visableType, Long projectId, Long moduleId, Long selModuleId,
                             int page, @RequestParam(defaultValue = "") String flag) {
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        if (configId == null) {
            //XXX add by kanguangwen
            //先查询当前配置是否存在  以projectId,configKey作为唯一条件查询
            if (!configService.configExist(projectId, configKey, true, null)) {//不存在 则添加
                configService.insertConfig(configKey, configValue, configDesc, configType, visableType, projectId,
                    moduleId, user.getUserCode());
            }
        } else {
            // add by kanguangwen
            if (!configService.configExist(projectId, configKey, false, configId)) {//不存在 则更新
                configService.updateConfig(type, configId, configKey, configValue, configDesc, configType, visableType,
                    projectId, moduleId, user.getUserCode());
            }
        }
        //注释自动推送
        //String projCode = (String) projectService.queryProject(projectId).get("PROJ_CODE");
        //String moduleName = moduleService.findName(moduleId);
        //diamondServerHandler.pushConfig(projCode, type, moduleName);
        if (selModuleId != null)
            return "redirect:/profile/" + type + "/" + projectId + "?moduleId=" + selModuleId + "&flag=" + flag;
        else
            return "redirect:/profile/" + type + "/" + projectId + "?page=" + page + "&flag=" + flag;
    }

    @RequestMapping("/config/delete/{id}")
    public String deleteConfig(String type, Long projectId, String moduleName, @PathVariable Long id) {
        configService.deleteConfig(id, projectId);

        //注释自动推送
        //String projCode = (String) projectService.queryProject(projectId).get("PROJ_CODE");
        //diamondServerHandler.pushConfig(projCode, type, moduleName);
        return "redirect:/profile/" + type + "/" + projectId;
    }

    @RequestMapping("/open/config/{projectCode}/{profile}")
    public void preview(@PathVariable("profile") String profile, @PathVariable("projectCode") String projectCode,
                        HttpServletRequest request, HttpServletResponse resp) {
    	String clientAddress=request.getParameter("clientAddress");
    	if(StringUtils.isBlank(clientAddress) || (!clientAddress.contains(":"))){
    		this.outputContent(resp, "{\"msg\":\"clientAddress格式不正确\",\"code\":\"-1\"}");
    		return;
    	}
    	if(!OpenValidator.isValid(projectCode,profile,(String)request.getParameter("cipherContent"))){//不是合法请求
        	this.outputContent(resp, "{\"msg\":\"合法性验证失败\",\"code\":\"-1\"}");
        	return;
        }
        
        //把当前连接信息存入数据库,以支持server端的分布式
        ShowPushData data = new ShowPushData();
        data.setProjectCode(projectCode);
        data.setProfileName(profile);
        data.setModuleNames("ALL");
        data.setServerUUID(DiamondServerHandler.SERVER_UUID);
        data.setClientAddress(clientAddress);
        //非java系统，ctxIdentifier都为-1
        data.setCtxIdentifier(-1);
        data.setConnTime(new Date());
        //默认不需要推送
        data.setNeedPush("0");
        data.setClientType(request.getParameter("clientType"));
        //持久化到DB
        showPushService.addShowPushDate(data);
        
        try {
            String format = "json";
            String config = configService.queryConfigs(projectCode, profile, format);

            if (format.equals("json"))
                resp.setContentType("application/json;charset=UTF-8");
            else
                resp.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println(config);
            out.flush();
            out.close();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                PrintWriter out = resp.getWriter();
                out.println("error = " + e.getMessage());
            } catch (IOException e1) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @RequestMapping("/open/heartBeat/{projectCode}/{profile}")
    public void heartBeat(@PathVariable("profile") String profile, @PathVariable("projectCode") String projectCode,HttpServletRequest request){
    	String clientAddress=request.getParameter("clientAddress");
    	
    	MessageBody body = new MessageBody();
    	body.setProjCode(projectCode);
    	body.setProfile(profile);
    	body.setClientAddress(clientAddress);
    	body.setClientType(request.getParameter("clientType"));
    	try{
    		showPushService.updateConnTimeByClientAddress(body, -1);
    	}catch(Exception e){
    		LOGGER.error("更新非java客户端的最后连接事件失败",e);
    	}
    }
    
    //@RequestMapping("/preview/{projectCode}/{module}/{type}")
    public void previewModule(@PathVariable("type") String type, @PathVariable("module") String modules,
                              @PathVariable("projectCode") String projectCode, HttpServletRequest request,
                              HttpServletResponse resp) {
        try {
            String format = request.getParameter("format");
            if (StringUtils.isBlank(format)) {
                format = "properties";
            }
            String[] moduleArr = StringUtils.split(modules, ",");
            String config = configService.queryConfigs(projectCode, moduleArr, type, format);

            if (format.equals("json"))
                resp.setContentType("application/json;charset=UTF-8");
            else
                resp.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println(config);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                PrintWriter out = resp.getWriter();
                out.println("error = " + e.getMessage());
            } catch (IOException e1) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    //@RequestMapping("/preview/{projectCode}/{module}/{key}/{type}")
    public void previewKey(@PathVariable("type") String type, @PathVariable("key") String key,
                           @PathVariable("module") String module, @PathVariable("projectCode") String projectCode,
                           HttpServletRequest request, HttpServletResponse resp) {
        try {
            String config = configService.queryValue(projectCode, module, key, type);
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            out.println(config);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                PrintWriter out = resp.getWriter();
                out.println("error = " + e.getMessage());
            } catch (IOException e1) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
