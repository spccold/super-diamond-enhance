package com.github.diamond.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.github.diamond.web.model.ModuleDetail;
import com.github.diamond.web.model.ModuleTemplate;

/**
 * 
 * 
 * @author 机冷
 * @version $Id: ModuleTemplateService.java, v 0.1 2015年3月20日 上午10:42:10
 *          kanguangwen Exp $
 */

@Service
public class ModuleTemplateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleTemplateService.class);

    @Autowired
    private ModuleService       moduleService;

    @Autowired
    private ConfigService       configService;

    @Autowired
    private JdbcTemplate        jdbcTemplate;

    /**
     * 导入module模板 在事物中 1.插入对应的moduleId 2.插入具体的配置项
     * 
     * @param templates
     */
    @Transactional
    public void importModuleTemplate(List<ModuleTemplate> templates, Long projectId, String userCode) {
        List<ModuleDetail> moduleDetails = null;
        if (!CollectionUtils.isEmpty(templates)) {
            for (ModuleTemplate template : templates) {
                String moduleName = template.getModuleName();
                long moduleId = -9;
                // 检查module是否存在
                if (moduleService.moduleExist(projectId, moduleName)) {// 如果存在找出moduleId
                    moduleId = moduleService.queryModuleIdByModuleNameAndProjectId(projectId, moduleName);
                    if (moduleId == -1) {
                        throw new RuntimeException("moduleId非法");
                    }
                } else {
                    // 插入module
                    moduleId = moduleService.save(projectId, moduleName);
                    if (moduleId == -2) {
                        throw new RuntimeException("ModuleName:ALL 为系统保留字段，不能添加");
                    }
                }

                moduleDetails = template.getModuleDetails();
                if (!CollectionUtils.isEmpty(moduleDetails)) {
                    for (ModuleDetail detail : moduleDetails) {
                        if (!configService.configExist(projectId, detail.getConfigKey(), true, null)) {// 不存在 则添加
                            // 插入具体的配置项
                            configService.insertConfig(detail.getConfigKey(), detail.getConfigValue(),
                                detail.getConfigDesc(), detail.getConfigType(), detail.getVisableType(), projectId,
                                moduleId, userCode);
                        } else {
                            throw new RuntimeException("ModuleName:" + moduleName + " ConfigKey:"
                                                       + detail.getConfigKey() + "已经存在，不能添加");
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加模块名称
     * 
     * @param moduleName
     */
    public int saveModuleName(String moduleName) {
        if (moduleExist(moduleName)) {
            //-1代表模块名称已存在
            return -1;
        }
        String sql = "insert into MODULE_TEMPLATE values(?)";
        try {
            jdbcTemplate.update(sql, moduleName);
        } catch (DataAccessException e) {
            LOGGER.error("保存模版名称失败", e);
        }
        return 0;
    }

    /**
     * 删除模块
     */
    @Transactional
    public void deleteModule(String moduleName) {
        String sql = "delete from MODULE_TEMPLATE where MODULE_NAME = ?";
        jdbcTemplate.update(sql, moduleName);
        sql = "delete from MODULE_TEMPLATE_DETAIL where MODULE_NAME = ?";
        jdbcTemplate.update(sql, moduleName);
    }

    /**
     * 检查模块名称是否存在
     * 
     * @param moduleName
     * @return
     */
    public boolean moduleExist(String moduleName) {
        String sql = "select count(*) from MODULE_TEMPLATE where MODULE_NAME = ?";
        Long ret = null;
        try {
            ret = jdbcTemplate.queryForObject(sql, Long.class, moduleName);
        } catch (DataAccessException e) {
            LOGGER.error("查询模版名称是否存在失败", e);
        }

        return (ret != null && ret >= 1);
    }

    /**
     * 
     * @return
     */
    public List<String> queryAllModuleNames(int offset, int limit) {
        String sql = "select MODULE_NAME from MODULE_TEMPLATE limit ?,?";
        List<String> moduleNames = null;
        try {
            moduleNames = jdbcTemplate.queryForList(sql, String.class, offset, limit);
        } catch (DataAccessException e) {
            LOGGER.error("查询所有的模块名称失败", e);
        }
        if (CollectionUtils.isEmpty(moduleNames)) {
            moduleNames = new ArrayList<String>();
        }
        return moduleNames;
    }

    public int queryCountForPager() {
        String sql = "select count(*) from MODULE_TEMPLATE";
        int count = 0;
        try {
            count = jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (DataAccessException e) {
            //do nothing
        }
        return count;
    }

    /**
     * 没有具体配置项的module不展示出来
     * 
     * @return
     */
    public List<String> queryAllModuleNamesForModuleList(int offset, int limit) {
        String sql = "select distinct(a.MODULE_NAME) from MODULE_TEMPLATE a, MODULE_TEMPLATE_DETAIL b where a.MODULE_NAME = b.MODULE_NAME and b.MODULE_NAME != ? limit ?,?";
        List<String> moduleNames = null;
        try {
            moduleNames = jdbcTemplate.queryForList(sql, String.class, StringUtils.EMPTY, offset, limit);
        } catch (DataAccessException e) {
            LOGGER.error("查询所有的模块名称失败", e);
        }
        if (CollectionUtils.isEmpty(moduleNames)) {
            moduleNames = new ArrayList<String>();
        }
        return moduleNames;
    }

    public int queryModuleListCountForPager() {
        String sql = "select count(distinct(a.MODULE_NAME)) from MODULE_TEMPLATE a, MODULE_TEMPLATE_DETAIL b where a.MODULE_NAME = b.MODULE_NAME and b.MODULE_NAME != ?";
        int count = 0;
        try {
            count = jdbcTemplate.queryForObject(sql, Integer.class, StringUtils.EMPTY);
        } catch (DataAccessException e) {
            //do nothing
        }
        return count;
    }

    public int saveModuleDeatil(String moduleName, String configKey, String configValue, String configDesc) {
        if (configKeyExistByModuleName(moduleName, configKey)) {
            //代表configKey已经存在
            return -1;
        }

        String sql = "insert into MODULE_TEMPLATE_DETAIL values(?,?,?,?)";
        try {
            jdbcTemplate.update(sql, moduleName, configKey, configValue, configDesc);
        } catch (DataAccessException e) {
            LOGGER.error("保存模版详情失败");
        }
        return 0;
    }

    /**
     * 检查单个模版下configKey是否存在重复
     * 
     * @param moduleName
     * @param configKey
     * @return
     */
    public boolean configKeyExistByModuleName(String moduleName, String configKey) {
        String sql = "select count(*) from MODULE_TEMPLATE_DETAIL where MODULE_NAME =? and CONFIG_KEY = ?";
        Long ret = null;
        try {
            ret = jdbcTemplate.queryForObject(sql, Long.class, moduleName, configKey);
        } catch (DataAccessException e) {
            //do nothing
        }
        return (ret != null && ret >= 1);
    }

    public void updateModuleDetailByConfigKey(String moduleName, String configKey, String configValue,
                                              String configDesc, String oldKey) {
        String sql = null;
        if (StringUtils.isBlank(oldKey)) {
            sql = "update MODULE_TEMPLATE_DETAIL set CONFIG_VALUE=?, CONFIG_DESC=? where MODULE_NAME=? and CONFIG_KEY=?";
            try {
                jdbcTemplate.update(sql, configValue, configDesc, moduleName, configKey);
            } catch (DataAccessException e) {
                //do nothing
            }
        } else {
            sql = "update MODULE_TEMPLATE_DETAIL set CONFIG_KEY =?, CONFIG_VALUE=?, CONFIG_DESC=? where MODULE_NAME=? and CONFIG_KEY=?";
            try {
                jdbcTemplate.update(sql, configKey, configValue, configDesc, moduleName, oldKey);
            } catch (DataAccessException e) {
                //do nothing
            }
        }
    }

    /**
     * 删除模块的一个配置
     * @param moduleName
     * @param configKey
     */
    public void deleteModuleDetail(String moduleName, String configKey) {
        String sql = "delete from MODULE_TEMPLATE_DETAIL where MODULE_NAME =? and CONFIG_KEY = ?";
        try {
            jdbcTemplate.update(sql, moduleName, configKey);
        } catch (DataAccessException e) {
            //do nothing
        }
    }

    public List<Map<String, Object>> queryModuleDetailByModuleName(String moduleName, int offset, int limit) {
        String sql = "select * from MODULE_TEMPLATE_DETAIL where MODULE_NAME = ? limit ?,?";
        try {
            return jdbcTemplate.queryForList(sql, moduleName, offset, limit);
        } catch (DataAccessException e) {
            LOGGER.error("查询模版详情列表", e);
            return new ArrayList<Map<String, Object>>();
        }
    }

    public int queryModuleDetailCountForPager(String moduleName) {
        String sql = "select count(*) from MODULE_TEMPLATE_DETAIL where MODULE_NAME = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, moduleName);
        } catch (DataAccessException e) {
            return 0;
        }
    }

    /**
     * 
     * @param moduleName
     * @return
     */
    public List<ModuleTemplate> queryAllConfigsByModuleName(String moduleName) {
        List<Map<String, Object>> allConfigs = null;

        String sql = "select * from MODULE_TEMPLATE_DETAIL where MODULE_NAME = ?";
        try {
            allConfigs = jdbcTemplate.queryForList(sql, moduleName);
        } catch (DataAccessException e) {
            //do nothing
        }
        List<ModuleTemplate> templates = null;
        if (!CollectionUtils.isEmpty(allConfigs)) {
            templates = new ArrayList<ModuleTemplate>();
            ModuleTemplate template = new ModuleTemplate();
            template.setModuleName((String) allConfigs.get(0).get("MODULE_NAME"));
            List<ModuleDetail> details = new ArrayList<ModuleDetail>();
            ModuleDetail detail = null;
            for (Map<String, Object> config : allConfigs) {
                detail = new ModuleDetail();
                detail.setConfigKey((String) config.get("CONFIG_KEY"));
                detail.setConfigValue((String) config.get("CONFIG_VALUE"));
                detail.setConfigDesc((String) config.get("CONFIG_DESC"));
                details.add(detail);
            }
            template.setModuleDetails(details);
            templates.add(template);
        }
        return templates;
    }
}
