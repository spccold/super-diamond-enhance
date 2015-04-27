package com.github.diamond.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ClientKeepAliveController {

    @RequestMapping("keepAlive")
    public void keepAlive(HttpServletRequest request) {
        /* String messageBody = request.getParameter("messageBody");
         MessageBody body = JsonUtils.objectFromJson(messageBody, MessageBody.class);*/
    }
}
