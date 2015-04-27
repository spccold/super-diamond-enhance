package com.github.diamond.web.model;

/**
 * 配置详细配置
 * 
 * @author 机冷
 * @version $Id: ModuleTemplate.java, v 0.1 2015年3月19日 下午7:03:06 kanguangwen Exp $
 */
public class ModuleDetail {
    private String configKey;
    private String configValue;
    private String configDesc;
    private String configType;
    private String visableType;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigDesc() {
        return configDesc;
    }

    public void setConfigDesc(String configDesc) {
        this.configDesc = configDesc;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getVisableType() {
        return visableType;
    }

    public void setVisableType(String visableType) {
        this.visableType = visableType;
    }

    @Override
    public String toString() {
        return "ModuleDetail [configKey=" + configKey + ", configValue=" + configValue + ", configDesc=" + configDesc
               + ", configType=" + configType + ", visableType=" + visableType + "]";
    }
}
