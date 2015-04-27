package com.github.diamond.web.model;

import java.util.List;

/**
 * 配置模板
 * 
 * @author 机冷
 * @version $Id: ModuleTemplate.java, v 0.1 2015年3月19日 下午7:03:06 kanguangwen Exp $
 */
public class ModuleTemplate {
    private String             moduleName;
    private List<ModuleDetail> moduleDetails;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public List<ModuleDetail> getModuleDetails() {
        return moduleDetails;
    }

    public void setModuleDetails(List<ModuleDetail> moduleDetails) {
        this.moduleDetails = moduleDetails;
    }
}
