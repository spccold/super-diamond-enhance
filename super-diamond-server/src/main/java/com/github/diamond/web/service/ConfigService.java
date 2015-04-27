/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.web.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.support.json.JSONUtils;
import com.github.diamond.web.model.ModuleDetail;
import com.github.diamond.web.model.ModuleTemplate;

/**
 * Create on @2013-8-23 @上午10:26:17 
 * @author bsli@ustcinfo.com
 */
@Service
public class ConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    private JdbcTemplate        jdbcTemplate;

    @Autowired
    private ProjectService      projectService;

    /**
     * 为手动推送查询单个配置项
     * 
     * @param projectCode
     * @param profileName
     * @param moduleName
     * @param configKey
     * @return
     */
    public String queryConfigsForMannul(String projectCode, String profileName, String moduleName, String configKey,
                                        String configValue) {
        List<Map<String, Object>> configs = null;
        if (StringUtils.isBlank(configValue)) {//全局
            String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c WHERE a.MODULE_ID = b.MODULE_ID"
                         + " AND a.PROJECT_ID=c.ID AND a.DELETE_FLAG =0 and c.DELETE_FLAG=0 AND c.PROJ_CODE=? and b.MODULE_NAME=? and a.CONFIG_KEY=?";
            try {
                configs = jdbcTemplate.queryForList(sql, projectCode, moduleName, configKey);
            } catch (DataAccessException e) {
                LOGGER.error("查询配置失败", e);
            }
        } else {//局部
                //局部推送,推送数据由页面传来，无须查询数据库
            configs = new ArrayList<Map<String, Object>>();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("CONFIG_KEY", configKey);
            map.put("CONFIG_DESC", "newValue");
            if ("development".equals(profileName)) {
                map.put("DEVELOPMENT_VERSION", "0");
                map.put("CONFIG_VALUE", configValue);
            } else if ("production".equals(profileName)) {
                map.put("PRODUCTION_VERSION", "0");
                map.put("PRODUCTION_VALUE", configValue);
            } else if ("test".equals(profileName)) {
                map.put("TEST_VERSION", "0");
                map.put("TEST_VALUE", configValue);
            } else if ("build".equals(profileName)) {
                map.put("BUILD_VERSION", "0");
                map.put("BUILD_VALUE", configValue);
            }

            configs.add(map);
        }

        return viewConfig(configs, profileName);
    }

    /**
     * 检查配置是否已经存在
     * 
     * @param projectId
     * @param configKey
     * @param insertCheck
     * @param configId
     * @return
     */
    public boolean configExist(Long projectId, String configKey, boolean insertCheck, Long configId) {
        if (insertCheck) {
            String sql = "select count(*) from CONF_PROJECT_CONFIG where CONFIG_KEY=? and PROJECT_ID=? and DELETE_FLAG = 0";
            Long ret = null;
            try {
                ret = jdbcTemplate.queryForObject(sql, Long.class, configKey, projectId);
            } catch (DataAccessException e) {
                LOGGER.error("查询配置的key是否已经存在", e);
                //异常，禁止用户添加
                return true;
            }
            return ret != null && ret >= 1;
        } else {
            String sql = "select CONFIG_ID from CONF_PROJECT_CONFIG where CONFIG_KEY=? and PROJECT_ID=? and DELETE_FLAG = 0";
            List<Integer> configIds = null;
            try {
                configIds = jdbcTemplate.queryForList(sql, Integer.class, configKey, projectId);
            } catch (DataAccessException e) {
                LOGGER.error("查询配置的key是否已经存在", e);
            }
            if (CollectionUtils.isEmpty(configIds)) {
                return false;
            } else {
                Integer config = configIds.get(0);
                return config.intValue() != configId.intValue();
            }
        }
    }

    public List<Map<String, Object>> queryConfigs(Long projectId, Long moduleId, int offset, int limit) {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? ";

        if (moduleId != null) {
            sql = sql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
            return jdbcTemplate.queryForList(sql, projectId, moduleId, offset, limit);
        } else {
            sql = sql + " order by a.MODULE_ID limit ?,?";
            return jdbcTemplate.queryForList(sql, projectId, offset, limit);
        }

    }

    public long queryConfigCount(String moduleName, String configKey, String configValue, String configDesc) {
        String sql = "SELECT count(*) FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND c.ID = b.PROJ_ID AND a.DELETE_FLAG =0";
        List<Object> args = new ArrayList<Object>();

        if (StringUtils.isNotBlank(moduleName)) {
            sql = sql + " AND b.MODULE_NAME like ?";
            args.add("%" + moduleName + "%");
        }
        if (StringUtils.isNotBlank(configKey)) {
            sql = sql + " AND a.CONFIG_KEY like ?";
            args.add("%" + configKey + "%");
        }
        if (StringUtils.isNotBlank(configValue)) {
            sql = sql + " AND a.CONFIG_VALUE like ?";
            args.add("%" + configValue + "%");
        }
        if (StringUtils.isNotBlank(configDesc)) {
            sql = sql + " AND a.CONFIG_DESC like ?";
            args.add("%" + configDesc + "%");
        }
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, args.toArray(new Object[args.size()]));
        } catch (DataAccessException e) {
            LOGGER.error("根据条件模糊查询所有配置的数量失败", e);
            return -1;
        }
    }

    public List<Map<String, Object>> queryAllConfigsByConditons(String moduleName, String configKey,
                                                                String configValue, String configDesc, int offset,
                                                                int limit) {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND c.ID = b.PROJ_ID AND a.DELETE_FLAG =0";
        List<Object> args = new ArrayList<Object>();

        if (StringUtils.isNotBlank(moduleName)) {
            sql = sql + " AND b.MODULE_NAME like ?";
            args.add("%" + moduleName + "%");
        }
        if (StringUtils.isNotBlank(configKey)) {
            sql = sql + " AND a.CONFIG_KEY like ?";
            args.add("%" + configKey + "%");
        }
        if (StringUtils.isNotBlank(configValue)) {
            sql = sql + " AND a.CONFIG_VALUE like ?";
            args.add("%" + configValue + "%");
        }
        if (StringUtils.isNotBlank(configDesc)) {
            sql = sql + " AND a.CONFIG_DESC like ?";
            args.add("%" + configDesc + "%");
        }
        sql = sql + " limit ?,?";
        args.add(offset);
        args.add(limit);

        try {
            return jdbcTemplate.queryForList(sql, args.toArray(new Object[args.size()]));
        } catch (DataAccessException e) {
            LOGGER.error("根据条件模糊查询所有配置项失败", e);
            return null;
        }
    }

    public List<ModuleTemplate> queryAllConfigsByProjectId(Long projectId, String profile) {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? ";
        List<Map<String, Object>> allConfigs = jdbcTemplate.queryForList(sql, projectId);

        ModuleDetail detail = null;
        List<ModuleDetail> details = null;
        Map<String, List<ModuleDetail>> temp = new HashMap<String, List<ModuleDetail>>();

        if (!CollectionUtils.isEmpty(allConfigs)) {
            for (Map<String, Object> singleConfig : allConfigs) {
                String moduleName = (String) singleConfig.get("MODULE_NAME");
                if (temp.containsKey(moduleName)) {//判断module是否已经存在
                    details = temp.get(moduleName);
                } else {
                    details = new ArrayList<ModuleDetail>();
                    temp.put(moduleName, details);
                }
                detail = new ModuleDetail();
                detail.setConfigKey((String) singleConfig.get("CONFIG_KEY"));
                if ("development".equals(profile)) {
                    detail.setConfigValue((String) singleConfig.get("CONFIG_VALUE"));
                } else if ("test".equals(profile)) {
                    detail.setConfigValue((String) singleConfig.get("TEST_VALUE"));
                } else if ("build".equals(profile)) {
                    detail.setConfigValue((String) singleConfig.get("BUILD_VALUE"));
                } else if ("production".equals(profile)) {
                    detail.setConfigValue((String) singleConfig.get("PRODUCTION_VALUE"));
                } else {
                    detail.setConfigValue("xxx");
                }
                detail.setConfigDesc((String) singleConfig.get("CONFIG_DESC"));
                detail.setConfigType((String) singleConfig.get("CONFIG_TYPE"));
                detail.setVisableType((String) singleConfig.get("VISABLE_TYPE"));
                details.add(detail);
            }
        }
        List<ModuleTemplate> templates = new ArrayList<ModuleTemplate>();
        ModuleTemplate template = null;
        if (!CollectionUtils.isEmpty(temp)) {
            for (Entry<String, List<ModuleDetail>> entry : temp.entrySet()) {
                template = new ModuleTemplate();
                template.setModuleName(entry.getKey());
                template.setModuleDetails(entry.getValue());
                templates.add(template);
            }
        }
        return templates;
    }

    public long queryConfigCount(Long projectId, Long moduleId) {
        String sql = "SELECT count(*) FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? ";

        if (moduleId != null) {
            sql = sql + " AND a.MODULE_ID = ? order by a.MODULE_ID";
            return jdbcTemplate.queryForObject(sql, Long.class, projectId, moduleId);
        } else {
            sql = sql + " order by a.MODULE_ID";
            return jdbcTemplate.queryForObject(sql, Long.class, projectId);
        }

    }

    public String queryConfigs(String projectCode, String type, String format) {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
        List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, projectCode);
        if ("php".equals(format)) {
            return viewConfigPhp(configs, type);
        } else if ("json".equals(format)) {
            return viewConfigJson(configs, type);
        } else
            return viewConfig(configs, type);
    }

    public String queryConfigsForScan(String projectCode, String type, String userCode) {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
        List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, projectCode);
        return viewConfigForScan(configs, type, userCode);
    }

    public String queryConfigs(String projectCode, String[] modules, String type, String format) {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=? "
                     + "AND b.MODULE_NAME in ('" + StringUtils.join(modules, "','") + "')";

        List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, projectCode);
        if ("php".equals(format)) {
            return viewConfigPhp(configs, type);
        } else if ("json".equals(format)) {
            return viewConfigJson(configs, type);
        } else
            return viewConfig(configs, type);
    }

    public String queryValue(String projectCode, String module, String key, String type) {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                     + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=? "
                     + "AND b.MODULE_NAME=? AND a.CONFIG_KEY=?";
        Map<String, Object> config = jdbcTemplate.queryForMap(sql, projectCode, module, key);
        if ("development".equals(type)) {
            return (String) config.get("CONFIG_VALUE");
        } else if ("production".equals(type)) {
            return (String) config.get("PRODUCTION_VALUE");
        } else if ("test".equals(type)) {
            return (String) config.get("TEST_VALUE");
        } else if ("build".equals(type)) {
            return (String) config.get("BUILD_VALUE");
        } else
            return "";
    }

    @Transactional
    public void insertConfig(String configKey, String configValue, String configDesc, String configType,
                             String visableType, Long projectId, Long moduleId, String user) {
        String sql = "SELECT MAX(CONFIG_ID)+1 FROM CONF_PROJECT_CONFIG";
        long id = 1;
        try {
            id = jdbcTemplate.queryForObject(sql, Long.class);
        } catch (NullPointerException e) {
            ;
        }

        sql = "INSERT INTO CONF_PROJECT_CONFIG(CONFIG_ID,CONFIG_KEY,CONFIG_VALUE,CONFIG_DESC,CONFIG_TYPE,VISABLE_TYPE,PROJECT_ID,MODULE_ID,DELETE_FLAG,OPT_USER,OPT_TIME,"
              + "PRODUCTION_VALUE,PRODUCTION_USER,PRODUCTION_TIME,TEST_VALUE,TEST_USER,TEST_TIME,BUILD_VALUE,BUILD_USER,BUILD_TIME) "
              + "VALUES (?,?,?,?,?,?,?,?,0,?,?,?,?,?,?,?,?,?,?,?)";
        Date time = new Date();
        jdbcTemplate.update(sql, id, configKey, configValue, configDesc, configType, visableType, projectId, moduleId,
            user, time, configValue, user, time, configValue, user, time, configValue, user, time);

        projectService.updateVersion(projectId);
    }

    @Transactional
    public void updateConfig(String type, Long configId, String configKey, String configValue, String configDesc,
                             String configType, String visableType, Long projectId, Long moduleId, String user) {
        if ("development".equals(type)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_KEY=?,CONFIG_VALUE=?,CONFIG_DESC=?,CONFIG_TYPE=?,VISABLE_TYPE=?,PROJECT_ID=?,MODULE_ID=?,OPT_USER=?,OPT_TIME=? where CONFIG_ID=?";
            jdbcTemplate.update(sql, configKey, configValue, configDesc, configType, visableType, projectId, moduleId,
                user, new Date(), configId);
            projectService.updateVersion(projectId, type);
        } else if ("production".equals(type)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_KEY=?,PRODUCTION_VALUE=?,CONFIG_DESC=?,PROJECT_ID=?,MODULE_ID=?,PRODUCTION_USER=?,PRODUCTION_TIME=? where CONFIG_ID=?";
            jdbcTemplate.update(sql, configKey, configValue, configDesc, projectId, moduleId, user, new Date(),
                configId);
            projectService.updateVersion(projectId, type);
        } else if ("test".equals(type)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_KEY=?,TEST_VALUE=?,CONFIG_DESC=?,PROJECT_ID=?,MODULE_ID=?,TEST_USER=?,TEST_TIME=? where CONFIG_ID=?";
            jdbcTemplate.update(sql, configKey, configValue, configDesc, projectId, moduleId, user, new Date(),
                configId);
            projectService.updateVersion(projectId, type);
        } else if ("build".equals(type)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_KEY=?,BUILD_VALUE=?,CONFIG_DESC=?,PROJECT_ID=?,MODULE_ID=?,BUILD_USER=?,BUILD_TIME=? where CONFIG_ID=?";
            jdbcTemplate.update(sql, configKey, configValue, configDesc, projectId, moduleId, user, new Date(),
                configId);
            projectService.updateVersion(projectId, type);
        }
    }

    @Transactional
    public void updateConfigByApi(Long projectId, String profileName, String configKey, String configValue) {
        int count = 0;
        if ("development".equals(profileName)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_VALUE=? where PROJECT_ID=? and CONFIG_KEY=?";
            count = jdbcTemplate.update(sql, configValue, projectId, configKey);
        } else if ("production".equals(profileName)) {
            String sql = "update CONF_PROJECT_CONFIG set PRODUCTION_VALUE=? where PROJECT_ID=? and CONFIG_KEY=?";
            count = jdbcTemplate.update(sql, configValue, projectId, configKey);
        } else if ("test".equals(profileName)) {
            String sql = "update CONF_PROJECT_CONFIG set TEST_VALUE=? where PROJECT_ID=? and CONFIG_KEY=?";
            count = jdbcTemplate.update(sql, configValue, projectId, configKey);
        } else if ("build".equals(profileName)) {
            String sql = "update CONF_PROJECT_CONFIG set BUILD_VALUE=? where PROJECT_ID=? and CONFIG_KEY=?";
            count = jdbcTemplate.update(sql, configValue, projectId, configKey);
        }
        if (count != 1) {//说明更新出现问题 
            throw new RuntimeException("api更新configValue异常,回滚异常更新");
        }
        projectService.updateVersion(projectId, profileName);
    }

    public void deleteConfig(Long id, Long projectId) {
        String sql = "update CONF_PROJECT_CONFIG set DELETE_FLAG=1 where CONFIG_ID=?";
        jdbcTemplate.update(sql, id);
        projectService.updateVersion(projectId);
    }

    private String viewConfig(List<Map<String, Object>> configs, String type) {
        String message = "";

        boolean versionFlag = true;

        if (!CollectionUtils.isEmpty(configs)) {
            for (Map<String, Object> map : configs) {
                if (versionFlag) {
                    if ("development".equals(type)) {
                        message += "#version = " + map.get("DEVELOPMENT_VERSION") + "\r\n";
                    } else if ("production".equals(type)) {
                        message += "#version = " + map.get("PRODUCTION_VERSION") + "\r\n";
                    } else if ("test".equals(type)) {
                        message += "#version = " + map.get("TEST_VERSION") + "\r\n";
                    } else if ("build".equals(type)) {
                        message += "#version = " + map.get("BUILD_VERSION") + "\r\n";
                    }

                    versionFlag = false;
                }

                String desc = (String) map.get("CONFIG_DESC");
                desc = desc.replaceAll("\r\n", " ");
                if (StringUtils.isNotBlank(desc))
                    message += "#" + desc + "\r\n";

                if ("development".equals(type)) {
                    message += map.get("CONFIG_KEY") + " = " + map.get("CONFIG_VALUE") + "\r\n";
                } else if ("production".equals(type)) {
                    message += map.get("CONFIG_KEY") + " = " + map.get("PRODUCTION_VALUE") + "\r\n";
                } else if ("test".equals(type)) {
                    message += map.get("CONFIG_KEY") + " = " + map.get("TEST_VALUE") + "\r\n";
                } else if ("build".equals(type)) {
                    message += map.get("CONFIG_KEY") + " = " + map.get("BUILD_VALUE") + "\r\n";
                }
            }
        }

        return message;
    }

    private String viewConfigForScan(List<Map<String, Object>> configs, String type, String userCode) {
        String message = "";

        boolean versionFlag = true;

        if (!CollectionUtils.isEmpty(configs)) {
            for (Map<String, Object> map : configs) {
                if (versionFlag) {
                    if ("development".equals(type)) {
                        message += "#version = " + map.get("DEVELOPMENT_VERSION") + "\r\n";
                    } else if ("production".equals(type)) {
                        message += "#version = " + map.get("PRODUCTION_VERSION") + "\r\n";
                    } else if ("test".equals(type)) {
                        message += "#version = " + map.get("TEST_VERSION") + "\r\n";
                    } else if ("build".equals(type)) {
                        message += "#version = " + map.get("BUILD_VERSION") + "\r\n";
                    }

                    versionFlag = false;
                }

                String desc = (String) map.get("CONFIG_DESC");
                desc = desc.replaceAll("\r\n", " ");
                if (StringUtils.isNotBlank(desc))
                    message += "#" + desc + "\r\n";

                if (!"admin".equals(userCode)) {
                    if ("PRIVATE".equals(map.get("VISABLE_TYPE"))) {
                        message += map.get("CONFIG_KEY") + " = " + "******" + "\r\n";
                        continue;
                    }
                }

                if ("development".equals(type)) {
                    message += map.get("CONFIG_KEY") + " = " + map.get("CONFIG_VALUE") + "\r\n";
                } else if ("production".equals(type)) {
                    message += map.get("CONFIG_KEY") + " = " + map.get("PRODUCTION_VALUE") + "\r\n";
                } else if ("test".equals(type)) {
                    message += map.get("CONFIG_KEY") + " = " + map.get("TEST_VALUE") + "\r\n";
                } else if ("build".equals(type)) {
                    message += map.get("CONFIG_KEY") + " = " + map.get("BUILD_VALUE") + "\r\n";
                }
            }
        }

        return message;
    }

    private String viewConfigPhp(List<Map<String, Object>> configs, String type) {
        String message = "<?php\r\n" + "return array(\r\n" + "\t//profile = " + type + "\r\n";

        boolean versionFlag = true;
        for (Map<String, Object> map : configs) {
            if (versionFlag) {
                if ("development".equals(type)) {
                    message += "\t//version = " + map.get("DEVELOPMENT_VERSION") + "\r\n";
                } else if ("production".equals(type)) {
                    message += "\t//version = " + map.get("PRODUCTION_VERSION") + "\r\n";
                } else if ("test".equals(type)) {
                    message += "\t//version = " + map.get("TEST_VERSION") + "\r\n";
                } else if ("build".equals(type)) {
                    message += "\t//version = " + map.get("BUILD_VERSION") + "\r\n";
                }

                versionFlag = false;
            }

            String desc = (String) map.get("CONFIG_DESC");
            if (StringUtils.isNotBlank(desc))
                message += "\t//" + desc + "\r\n";

            if ("development".equals(type)) {
                message += "\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("CONFIG_VALUE"));
            } else if ("production".equals(type)) {
                message += "\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("PRODUCTION_VALUE"));
            } else if ("test".equals(type)) {
                message += "\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("TEST_VALUE"));
            } else if ("build".equals(type)) {
                message += "\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("BUILD_VALUE"));
            }
        }

        message += ");\r\n";

        return message;
    }

    private String viewConfigJson(List<Map<String, Object>> configs, String type) {
        Map<String, Object> confMap = new LinkedHashMap<String, Object>();
        boolean versionFlag = true;
        for (Map<String, Object> map : configs) {
            if (versionFlag) {
                if ("development".equals(type)) {
                    confMap.put("version", map.get("DEVELOPMENT_VERSION"));
                } else if ("production".equals(type)) {
                    confMap.put("version", map.get("PRODUCTION_VERSION"));
                } else if ("test".equals(type)) {
                    confMap.put("version", map.get("TEST_VERSION"));
                } else if ("build".equals(type)) {
                    confMap.put("version", map.get("BUILD_VERSION"));
                }

                versionFlag = false;
            }

            if ("development".equals(type)) {
                confMap.put(map.get("CONFIG_KEY").toString(), map.get("CONFIG_VALUE"));
            } else if ("production".equals(type)) {
                confMap.put(map.get("CONFIG_KEY").toString(), map.get("PRODUCTION_VALUE"));
            } else if ("test".equals(type)) {
                confMap.put(map.get("CONFIG_KEY").toString(), map.get("TEST_VALUE"));
            } else if ("build".equals(type)) {
                confMap.put(map.get("CONFIG_KEY").toString(), map.get("BUILD_VALUE"));
            }
        }

        return JSONUtils.toJSONString(confMap);
    }

    private String convertType(Object value) {
        String conf = String.valueOf(value).trim();
        if ("true".equals(conf) || "false".equals(conf)) {
            return conf + ",\r\n";
        } else if (isNumeric(conf)) {
            return conf + ",\r\n";
        } else {
            return "'" + conf + "',\r\n";
        }
    }

    public final static boolean isNumeric(String s) {
        if (s != null && !"".equals(s.trim()))
            return s.matches("^[0-9]*$");
        else
            return false;
    }
}
