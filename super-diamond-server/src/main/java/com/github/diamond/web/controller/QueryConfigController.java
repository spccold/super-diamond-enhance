package com.github.diamond.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.diamond.utils.PageUtil;
import com.github.diamond.web.service.ConfigService;

@Controller
public class QueryConfigController {

    private static final String QUERY_SUFFIX = "Query";
    private static final int    LIMIT        = 10;

    @Autowired
    private ConfigService       configService;

    @RequestMapping("query/{profile}")
    public String queryConfig(@RequestParam(defaultValue = "1") int page, @PathVariable("profile") String profile,
                              ModelMap modelMap, String moduleName, String configKey, String configValue,
                              String configDesc) {
        modelMap.addAttribute(
            "configs",
            configService.queryAllConfigsByConditons(moduleName, configKey, configValue, configDesc,
                PageUtil.getOffset(page, LIMIT), LIMIT));
        long recordCount = configService.queryConfigCount(moduleName, configKey, configValue, configDesc);
        modelMap.addAttribute("totalPages", PageUtil.pageCount(recordCount, LIMIT));
        modelMap.addAttribute("currentPage", page);
        modelMap.addAttribute("type", profile);
        modelMap.addAttribute("moduleName", moduleName);
        modelMap.addAttribute("configKey", configKey);
        modelMap.addAttribute("configValue", configValue);
        modelMap.addAttribute("configDesc", configDesc);
        return "query/" + profile + QUERY_SUFFIX;
    }

    @RequestMapping("queryConfig")
    public String configProfileIndex() {
        return "queryConfig";
    }
}
