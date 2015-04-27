package com.github.diamond.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.diamond.utils.PageUtil;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.ModuleDetail;
import com.github.diamond.web.model.ModuleTemplate;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ModuleTemplateService;

@Controller
public class ModuleManageController extends BaseController{
	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManageController.class);
	
	private static final int INDEX_LIMIT = 5;
	private static final int DETAIL_LIMIT = 10;
	
	@Autowired
	private ModuleTemplateService moduleService;

	@RequestMapping("moduleManage/index")
	public String index(
			@RequestParam(defaultValue = "1") int page,ModelMap model) {
		// 查询所有的模块名称
		model.addAttribute("modules", moduleService.queryAllModuleNames(PageUtil.getOffset(page, INDEX_LIMIT), INDEX_LIMIT));
		model.addAttribute("totalPages",
				PageUtil.pageCount(moduleService.queryCountForPager(), INDEX_LIMIT));
		model.addAttribute("currentPage", page);
		return "moduleManage";
	}

	@RequestMapping("moduleManage/save")
	public String saveModuleName(String moduleName, HttpSession session,
			ModelMap model) {
		// 保存模块
		int ret = moduleService.saveModuleName(moduleName);
		if (ret == -1) {
			session.setAttribute("message", "相同的模块名已经存在，不能添加！");
		}
		// 查询所以的模块名称
		model.addAttribute("totalPages",
				PageUtil.pageCount(moduleService.queryCountForPager(), INDEX_LIMIT));
		model.addAttribute("currentPage", 1);
		model.addAttribute("modules", moduleService.queryAllModuleNames(0,INDEX_LIMIT));
		return "moduleManage";
	}
	
	@RequestMapping("moduleManage/delete")
	public String deleteModule(ModelMap model, String moduleName){
		//删除模版以及该模版下的详细配置
		moduleService.deleteModule(moduleName);
		// 查询所以的模块名称
		model.addAttribute("totalPages",
						PageUtil.pageCount(moduleService.queryCountForPager(), INDEX_LIMIT));
		model.addAttribute("currentPage", 1);
		model.addAttribute("modules", moduleService.queryAllModuleNames(0,INDEX_LIMIT));
		return "moduleManage";
	}
	
	@RequestMapping("moduleManage/detail")
	public String moduleDetail(@RequestParam(defaultValue = "1") int page,ModelMap model, String moduleName){
		model.addAttribute("moduleName", moduleName);
		//分页查询模版列表
		model.addAttribute("moduleDetails", moduleService.queryModuleDetailByModuleName(moduleName, PageUtil.getOffset(page, DETAIL_LIMIT), DETAIL_LIMIT));
		model.addAttribute("totalPages",
				PageUtil.pageCount(moduleService.queryModuleDetailCountForPager(moduleName), DETAIL_LIMIT));
		model.addAttribute("currentPage", page); 
		return "template/templateDetail";
	}
	
	@RequestMapping("moduleManage/detail/save")
	public String saveModuleDetail(ModelMap model,String moduleName,String configKey,String configValue,String configDesc,String edit,String oldKey){
		if(StringUtils.isNotBlank(edit)){//编辑
			//判断configKey是否发生变化
			if(configKey.equals(oldKey)){//没变,直接更新
				moduleService.updateModuleDetailByConfigKey(moduleName, configKey, configValue, configDesc, oldKey);
			}else{//变化，先检查configKey是否已经存在
				if(moduleService.configKeyExistByModuleName(moduleName, configKey)){
					model.addAttribute("message", configKey+"已经存在，不允许重复添加");
				}else{//执行更新
					moduleService.updateModuleDetailByConfigKey(moduleName, configKey, configValue, configDesc, oldKey);
				}
			}
		}else{
			//保存模版详情
			int ret = moduleService.saveModuleDeatil(moduleName, configKey, configValue, configDesc);
			if(ret == -1){
				model.addAttribute("message", configKey+"已经存在，不允许重复添加");
			}
		}

		model.addAttribute("moduleName", moduleName);
		//分页查询模版列表
		model.addAttribute("moduleDetails", moduleService.queryModuleDetailByModuleName(moduleName, PageUtil.getOffset(1, DETAIL_LIMIT), DETAIL_LIMIT));
		model.addAttribute("totalPages",
				PageUtil.pageCount(moduleService.queryModuleDetailCountForPager(moduleName), DETAIL_LIMIT));
		model.addAttribute("currentPage", 1); 
		return "template/templateDetail";
	}
	
	@RequestMapping("moduleManage/detail/delete")
	public String deleteModuleDetail(ModelMap model, String moduleName,String configKey){
		//删除模版的一条纪录
		moduleService.deleteModuleDetail(moduleName, configKey);
		
		model.addAttribute("moduleName", moduleName);
		//分页查询模版列表
		model.addAttribute("moduleDetails", moduleService.queryModuleDetailByModuleName(moduleName, PageUtil.getOffset(1, DETAIL_LIMIT), DETAIL_LIMIT));
		model.addAttribute("totalPages",
				PageUtil.pageCount(moduleService.queryModuleDetailCountForPager(moduleName), DETAIL_LIMIT));
		model.addAttribute("currentPage", 1); 
		return "template/templateDetail";
	}
	
	@RequestMapping("moduleManage/moduleList")
	public String showModuleList(@RequestParam(defaultValue = "1") int page,ModelMap model,Long projectId) {
		// 查询所有的模块名称
		model.addAttribute("projectId", projectId);
		model.addAttribute("modules", moduleService.queryAllModuleNamesForModuleList(PageUtil.getOffset(page, INDEX_LIMIT), INDEX_LIMIT));
		model.addAttribute("totalPages",
				PageUtil.pageCount(moduleService.queryModuleListCountForPager(), INDEX_LIMIT));
		model.addAttribute("currentPage", page);
		return "moduleList";
	}
	
	@RequestMapping("moduleManage/importModule")
	public void importModule(Long projectId,String moduleName,HttpServletResponse response){
		User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
		List<ModuleTemplate> templates =  moduleService.queryAllConfigsByModuleName(moduleName);
		String msg = "模版导入成功";
		try {
            moduleService.importModuleTemplate(templates, projectId, user.getUserCode());
        } catch (Exception e) {
            LOGGER.error("导入Module模板文件失败", e);
            //返回错误页面
            msg = "模版导入失败，原因: "+e.getMessage();
        }
		this.outputContent(response, msg);
	}
	
	@RequestMapping("moduleManage/preview")
	public void previewModule(ModelMap model,HttpServletResponse response, String moduleName){
		List<ModuleTemplate> templates =  moduleService.queryAllConfigsByModuleName(moduleName);
		StringBuilder builder = new StringBuilder();
		if(!CollectionUtils.isEmpty(templates)){
			List<ModuleDetail> details = templates.get(0).getModuleDetails();
			for(ModuleDetail detail : details){
				builder.append(detail.getConfigKey()+" = "+detail.getConfigValue()+"\r\n");
			}
		}
		model.addAttribute("moduleName", moduleName);
		
		this.outputContent(response, builder.toString());
	}
}
