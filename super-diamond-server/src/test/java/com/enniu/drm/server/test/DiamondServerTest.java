package com.enniu.drm.server.test;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class DiamondServerTest {
    private static JdbcTemplate               jdbcTemplate;
    private static AbstractApplicationContext context;

    @BeforeClass
    public static void beforeClass() {
        context = new ClassPathXmlApplicationContext("spring.xml");
        jdbcTemplate = context.getBean(JdbcTemplate.class);
        if (null == jdbcTemplate) {
            throw new RuntimeException("jdbcTemplate is null!");
        }
    }

    @Test
    public void test() {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c WHERE a.MODULE_ID = b.MODULE_ID"
                     + " AND a.PROJECT_ID=c.ID AND a.DELETE_FLAG =0 and c.DELETE_FLAG =0 AND c.PROJ_CODE=? and b.MODULE_NAME=? and a.CONFIG_KEY=?";
        List<Map<String, Object>> configs = null;
        try {
            configs = jdbcTemplate.queryForList(sql, "001", "redis", "host");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        System.out.println(viewConfig(configs, "production"));
    }

    private String viewConfig(List<Map<String, Object>> configs, String type) {
        String message = "";

        boolean versionFlag = true;
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

        return message;
    }

    @AfterClass
    public static void afterClass() {
        //关闭Spring Container
        context.close();
    }
}
