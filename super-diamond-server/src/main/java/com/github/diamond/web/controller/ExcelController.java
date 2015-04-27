package com.github.diamond.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.github.diamond.utils.ExcelUtils;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.ModuleTemplate;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ModuleTemplateService;

@Controller
public class ExcelController {
    private static final Logger   LOGGER = LoggerFactory.getLogger(ExcelController.class);

    @Autowired
    private ModuleTemplateService moduleTemplateService;

    @Autowired
    private ConfigService         configService;

    @RequestMapping(value = "/importModule")
    public String handleFormUpload(@RequestParam("file") MultipartFile file, ModelMap model, String type, Long projectId) {
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        List<ModuleTemplate> templates = null;
        try {
            templates = ExcelUtils.getTemplateFromExcel(file.getInputStream());
            moduleTemplateService.importModuleTemplate(templates, projectId, user.getUserCode());
        } catch (Exception e) {
            LOGGER.error("上传Module模板文件失败", e);
            //返回错误页面
            model.addAttribute("errorMsg", e.getMessage());
            return "error_fileupload";
        }

        return "redirect:/profile/" + type + "/" + projectId;
    }

    @RequestMapping("/exportAllConfig/{profile}")
    public void configDownload(HttpServletResponse response, Long projectId, @PathVariable("profile") String profile) {
        response.setHeader("content-disposition", "attachment;filename=cs_export.xlsx");
        response.setContentType("application/octet-stream; charset=utf-8");
        OutputStream os = null;
        try {
            List<ModuleTemplate> templates = configService.queryAllConfigsByProjectId(projectId, profile);
            os = response.getOutputStream();
            Workbook wb = ExcelUtils.exportExcelFromModuleTemplate(templates);
            //输出Excel内容
            wb.write(os);
            os.flush();
        } catch (Exception e) {
            LOGGER.error("配置导出失败", e);
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                //do nothing
            }
        }
    }
}
