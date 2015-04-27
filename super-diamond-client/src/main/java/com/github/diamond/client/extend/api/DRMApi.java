package com.github.diamond.client.extend.api;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.diamond.client.extend.api.model.ClientInfoResp;
import com.github.diamond.client.extend.utils.HttpUtils;
import com.github.diamond.client.util.JsonUtils;
import com.google.common.base.Preconditions;

public class DRMApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(DRMApi.class);

    /**服务器域名 例如:http://pzzxj.51test.top/superdiamond*/
    private String              serverDomain;

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    /**
     * 查询当前配置的客户端列表
     * 
     * @param projectCode 项目编号
     * @param profileName 环境(development/build/test/production)
     * @param moduleName  模块名称，如果没有可以为null
     * @return
     */
    public List<ClientInfoResp> listClientInfos(String projectCode, String profileName, String moduleName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(projectCode), "projectCode is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(profileName), "profileName is null or empty");

        moduleName = (StringUtils.isBlank(moduleName) ? "ALL" : moduleName);
        String domainSuffix = "/api/{0}/{1}/{2}";
        String realDataUrl = this.serverDomain
                             + MessageFormat.format(domainSuffix, projectCode, profileName, moduleName);
        String jsonData = HttpUtils.post(null, realDataUrl);
        List<ClientInfoResp> clients = null;
        try {
            if (StringUtils.isNotBlank(jsonData)) {
                clients = JsonUtils.objectFromJson(jsonData, new TypeReference<List<ClientInfoResp>>() {
                });
            }
        } catch (Throwable e) {
            LOGGER.error("获取DRM系统的客户端地址失败", e);
        }
        return clients;
    }

    /**
     * 局部推送
     * 
     * @param projectCode 项目编号
     * @param profileName 环境(development/build/test/production)
     * @param moduleName  模块名称
     * @param clientAddress 需要推送的客户端地址
     * @param configKey    DRM对应的key
     * @param configValue  DRM对应的value
     * @return  推送是否成功
     */
    public boolean partPush(String projectCode, String profileName, String moduleName, String clientAddress,
                            String configKey, String configValue) {
        Preconditions.checkArgument(StringUtils.isNotBlank(projectCode), "projectCode is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(profileName), "profileName is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(moduleName), "moduleName is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(clientAddress), "clientAddress is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(configKey), "configKey is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(configValue), "configValue is null or empty");

        String domainSuffix = "/api/partPush/{0}/{1}/{2}";
        String realDataUrl = this.serverDomain
                             + MessageFormat.format(domainSuffix, projectCode, profileName, moduleName);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("clientAddress", clientAddress));
        nameValuePairs.add(new BasicNameValuePair("configKey", configKey));
        nameValuePairs.add(new BasicNameValuePair("configValue", configValue));
        String result = HttpUtils.post(nameValuePairs, realDataUrl);
        if (StringUtils.isNotBlank(result) && "SUCCESS".equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 全局推送
     * 
     * @param projectCode 项目编号
     * @param profileName 环境(development/build/test/production)
     * @param moduleName  模块名称
     * @param configKey   DRM对应的key
     * @param configValue DRM对应的value
     * @return 推送是否成功
     */
    public boolean globalPush(String projectCode, String profileName, String moduleName, String configKey,
                              String configValue) {
        Preconditions.checkArgument(StringUtils.isNotBlank(projectCode), "projectCode is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(profileName), "profileName is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(moduleName), "moduleName is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(configKey), "configKey is null or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(configValue), "configValue is null or empty");

        //1.更新数据库中的值
        if (update(projectCode, profileName, configKey, configValue)) {
            //2.执行全局推送
            if (!push(projectCode, profileName, moduleName, configKey)) {
                LOGGER.error("调用api进行全局推送失败");
            }
        }
        return false;
    }

    private boolean update(String projectCode, String profileName, String configKey, String configValue) {
        String domainSuffix = "/api/updateConfig/{0}/{1}";
        String realDataUrl = this.serverDomain + MessageFormat.format(domainSuffix, projectCode, profileName);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("configKey", configKey));
        nameValuePairs.add(new BasicNameValuePair("configValue", configValue));
        String result = HttpUtils.post(nameValuePairs, realDataUrl);
        if (StringUtils.isNotBlank(result) && "SUCCESS".equals(result)) {
            return true;
        }
        return false;
    }

    private boolean push(String projectCode, String profileName, String moduleName, String configKey) {
        String domainSuffix = "/api/globalPush/{0}/{1}/{2}";
        String realDataUrl = this.serverDomain
                             + MessageFormat.format(domainSuffix, projectCode, profileName, moduleName);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("configKey", configKey));
        String result = HttpUtils.post(nameValuePairs, realDataUrl);
        if (StringUtils.isNotBlank(result) && "SUCCESS".equals(result)) {
            return true;
        }
        return false;
    }

}