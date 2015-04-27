/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.web.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Create on @2013-8-21 @下午8:18:44 
 * @author bsli@ustcinfo.com
 */
@Service
public class ModuleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleService.class);

    @Autowired
    private JdbcTemplate        jdbcTemplate;

    public boolean moduleExist(long projectId, String moduleName) {
        String sql = "select count(*) from CONF_PROJECT_MODULE where PROJ_ID = ? and MODULE_NAME = ?";
        Long ret = null;
        try {
            ret = jdbcTemplate.queryForObject(sql, Long.class, projectId, moduleName);
        } catch (DataAccessException e) {
            LOGGER.error("查询module是否存在失败", e);
        }
        return (ret != null && ret >= 1);
    }

    public int queryModuleIdByModuleNameAndProjectId(long projectId, String moduleName) {
        String sql = "select MODULE_ID from CONF_PROJECT_MODULE where PROJ_ID = ? and MODULE_NAME = ?";
        List<Integer> moduleIds = null;
        try {
            moduleIds = jdbcTemplate.queryForList(sql, Integer.class, projectId, moduleName);
        } catch (DataAccessException e) {
            LOGGER.error("查询moduleId失败", e);
        }
        if (!CollectionUtils.isEmpty(moduleIds)) {
            return moduleIds.get(0);
        }
        return -1;
    }

    public List<Map<String, Object>> queryModules(long projectId) {
        String sql = "SELECT * FROM CONF_PROJECT_MODULE a WHERE a.PROJ_ID = ? order by a.MODULE_ID";
        return jdbcTemplate.queryForList(sql, projectId);
    }

    @Transactional
    public long save(Long projectId, String name) {
        //ALL为保留字段
        if ("ALL".equals(name)) {
            return -2;
        }

        //首先检验该moduleName是否已经存在
        String sql = "select count(*) from CONF_PROJECT_MODULE where module_name = ? and PROJ_ID = ?";
        boolean moduleNameExist = false;
        try {
            moduleNameExist = jdbcTemplate.queryForObject(sql, Long.class, name, projectId) > 0 ? true : false;
        } catch (DataAccessException e) {
            //do nothing
        }
        if (moduleNameExist) {
            //说明moduleName已经存在
            return -1;
        }

        sql = "SELECT MAX(MODULE_ID)+1 FROM CONF_PROJECT_MODULE";
        long id = 1;
        try {
            id = jdbcTemplate.queryForObject(sql, Long.class);
        } catch (NullPointerException e) {
            ;
        }
        sql = "INSERT INTO CONF_PROJECT_MODULE(MODULE_ID, PROJ_ID, MODULE_NAME) values(?, ?, ?)";
        jdbcTemplate.update(sql, id, projectId, name);
        return id;
    }

    public String findName(Long moduleId) {
        String sql = "SELECT module_name FROM CONF_PROJECT_MODULE WHERE module_id=?";
        return jdbcTemplate.queryForObject(sql, String.class, moduleId);
    }

    @Transactional
    public boolean delete(long moduleId, long projectId) {
        //String sql = "select count(*) from CONF_PROJECT_CONFIG where MODULE_ID = ? and PROJECT_ID = ?";
        //XXX edit by kanguangwen
        String sql = "select count(*) from CONF_PROJECT_CONFIG where MODULE_ID = ? and PROJECT_ID = ? and delete_flag = 0";

        int count = jdbcTemplate.queryForObject(sql, Integer.class, moduleId, projectId);
        if (count == 0) {
            sql = "delete from CONF_PROJECT_MODULE where MODULE_ID = ? and PROJ_ID = ?";
            jdbcTemplate.update(sql, moduleId, projectId);
            return true;
        } else {
            return false;
        }
    }
}
