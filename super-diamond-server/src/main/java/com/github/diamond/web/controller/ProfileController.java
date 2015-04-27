/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.diamond.utils.JsonUtils;
import com.github.diamond.utils.PageUtil;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.ClientInfoResp;
import com.github.diamond.web.model.ShowPushData;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ModuleService;
import com.github.diamond.web.service.ProjectService;
import com.github.diamond.web.service.ShowPushService;

/**
 * Create on @2013-8-21 @下午6:55:09 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ProfileController extends BaseController {
    @Autowired
    private ModuleService    moduleService;
    @Autowired
    private ConfigService    configService;
    @Autowired
    private ProjectService   projectService;
    @Autowired
    private ShowPushService  pushService;

    private static final int LIMIT = 10;

    @RequestMapping("/profile/{type}/{projectId}")
    public String profile(@PathVariable("type") String type, @PathVariable("projectId") Long projectId, Long moduleId,
                          ModelMap modelMap, @RequestParam(defaultValue = "1") int page) {
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        long userId = user.getId();
        modelMap.addAttribute("modules", moduleService.queryModules(projectId));
        modelMap.addAttribute("configs",
            configService.queryConfigs(projectId, moduleId, PageUtil.getOffset(page, LIMIT), LIMIT));
        modelMap.addAttribute("moduleId", moduleId);
        modelMap.addAttribute("project", projectService.queryProject(projectId));
        //判断当前访问profile的用户是不是该项目的拥有者
        modelMap.addAttribute("isOwner", projectService.queryOwnerIdByProjectId(projectId) == userId);
        modelMap.addAttribute("isAdmin", user.getUserCode().equals("admin"));
        long recordCount = configService.queryConfigCount(projectId, moduleId);
        modelMap.addAttribute("totalPages", PageUtil.pageCount(recordCount, LIMIT));
        modelMap.addAttribute("currentPage", page);

        return "profile/" + type;
    }

    @RequestMapping("/profile/preview/{projectCode}/{type}")
    public String preview(@PathVariable("type") String type, @PathVariable("projectCode") String projectCode,
                          Long projectId, ModelMap modelMap) {
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        String config = configService.queryConfigsForScan(projectCode, type, user.getUserCode());

        modelMap.addAttribute("project", projectService.queryProject(projectId));
        modelMap.addAttribute("message", config);
        return "profile/preview";
    }

    @RequestMapping("/profile/{projectId}/{profileName}/{moduleName}")
    public void getConfigClientInfos(@PathVariable("") Long projectId, @PathVariable("profileName") String prfileName,
                                     @PathVariable("moduleName") String moduleName, HttpServletResponse response) {

        String projectCode = projectService.queryProjectCodeById(projectId);
        ShowPushData data = new ShowPushData();
        data.setProjectCode(projectCode);
        data.setProfileName(prfileName);
        data.setModuleNames(moduleName);
        List<ClientInfoResp> resps = pushService.showClintInfos(data);
        //查询结果转化成json串返回客户端，用于展示
        String content = JsonUtils.jsonFromObject(resps);
        this.outputContent(response, content);
    }
}
