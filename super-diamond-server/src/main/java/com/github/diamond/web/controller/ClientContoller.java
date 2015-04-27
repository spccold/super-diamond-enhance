/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.web.controller;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.diamond.netty.DiamondServerHandler;
import com.github.diamond.netty.DiamondServerHandler.ClientInfo;
import com.github.diamond.netty.DiamondServerHandler.ClientKey;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ProjectService;

/**
 * Create on @2013-12-18 @上午11:44:10
 * 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ClientContoller extends BaseController {

	private static final String DATEFORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

	@Autowired
	private ProjectService projectService;

	@RequestMapping("/queryClients")
	public void queryClients(ModelMap modelMap) {

		User user = (User) SessionHolder.getSession().getAttribute(
				"sessionUser");
		boolean isAdmin = user.getUserCode().equals("admin");
		List<Project> projects = null;
		if (!isAdmin) {
			// 不作分页,查询当前用户所管理的项目配置以及对于的profile
			projects = projectService.queryProjectForUserNoPager(user);

			if (!CollectionUtils.isEmpty(projects)) {
				for (Project project : projects) {
					List<String> roles = projectService.queryRoles(
							project.getId(), user.getId());
					project.setRoles(roles);
				}
			}

		}

		// 当前用户所看到的客户端链接情况应当是只针对自己的当前项目
		List<Map<String, String>> clients = new ArrayList<Map<String, String>>();
		for (Entry<ClientKey, List<ClientInfo>> entry : DiamondServerHandler.clients
				.entrySet()) {
			ClientKey key = entry.getKey();
			// 项目编号
			String projcode = key.getProjCode();
			// profile
			String profile = key.getProfile();

			if (isAdmin) {
				String modules = StringUtils.join(key.getModuleArr());

				for (ClientInfo info : entry.getValue()) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("projcode", projcode);
					map.put("modules", modules);
					map.put("profile", profile);
					//map.put("address", info.getAddress().substring(1));
					map.put("connectTime", new SimpleDateFormat(
							DATEFORMAT_STRING).format(info.getConnectTime()));
					clients.add(map);
				}
			} else if (!CollectionUtils.isEmpty(projects)) {
				for (Project project : projects) {
					if (projcode.equals(project.getCode())
							&& !CollectionUtils.isEmpty(project.getRoles())
							&& project.getRoles().contains(profile)) {

						String modules = StringUtils.join(key.getModuleArr());

						for (ClientInfo info : entry.getValue()) {
							Map<String, String> map = new HashMap<String, String>();
							map.put("projcode", projcode);
							map.put("modules", modules);
							map.put("profile", profile);
							//map.put("address", info.getAddress().substring(1));
							map.put("connectTime", new SimpleDateFormat(
									DATEFORMAT_STRING).format(info
									.getConnectTime()));
							clients.add(map);
						}
					}
				}
			}
		}

		modelMap.addAttribute("clients", clients);
	}
}
