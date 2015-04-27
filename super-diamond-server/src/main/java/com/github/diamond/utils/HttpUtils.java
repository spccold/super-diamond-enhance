package com.github.diamond.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.github.diamond.jetty.JettyServer;
import com.github.diamond.web.model.HttpPushResponse;

/**
 * 
 * 
 * @author kangaungwen
 * @version $Id: HttpUtils.java, v 0.1 2015年2月4日 下午2:31:01 kanguangwen Exp $
 */
public class HttpUtils {
    /**初始化日志*/
    private static final Logger              LOGGER               = LoggerFactory.getLogger(HttpUtils.class);

    private static final String              HTTP_PREFIX          = "http://";
    /**获取应用上下文名称*/
    private static final String              projectContextName   = JettyServer.CONTEXTPATH;

    private static final String              CONFIG_KEY           = "configKey";
    private static final String              MODULE_NAME          = "moduleName";
    private static final String              CLIENT_ADDRESS       = "clientAddres";
    private static final String              CONFIG_VALUE         = "configValue";

    //各种超时设置
    private static final RequestConfig       defaultRequestConfig = RequestConfig.custom().setSocketTimeout(2000)
                                                                      .setConnectTimeout(2000)
                                                                      .setConnectionRequestTimeout(2000)
                                                                      .setStaleConnectionCheckEnabled(true).build();

    // 创建默认的httpClient实例.    
    private static final CloseableHttpClient httpclient           = HttpClients.custom()
                                                                      .setDefaultRequestConfig(defaultRequestConfig)
                                                                      .build();

    /**
     * 
     * 
     * @param configMaps
     */
    public static List<HttpPushResponse> post(String clientAddres, String moduleName, String configKey,
                                              String configValue, List<String> addresses) {
        if (CollectionUtils.isEmpty(addresses)) {
            return null;
        }

        HttpPost httppost = null;
        List<HttpPushResponse> responses = new ArrayList<HttpPushResponse>();
        HttpPushResponse pushResponse = null;

        List<NameValuePair> FORMS = new ArrayList<NameValuePair>();
        FORMS.add(new BasicNameValuePair(CONFIG_KEY, configKey));
        FORMS.add(new BasicNameValuePair(CONFIG_VALUE, configValue));
        FORMS.add(new BasicNameValuePair(MODULE_NAME, moduleName));
        FORMS.add(new BasicNameValuePair(CLIENT_ADDRESS, clientAddres));
        for (String address : addresses) {
            boolean pushSuccess = true;
            // 创建httppost    
            httppost = new HttpPost(HTTP_PREFIX + address + projectContextName + "/heartbeat");
            //设置超时
            httppost.setConfig(defaultRequestConfig);
            try {
                httppost.setEntity(new UrlEncodedFormEntity(FORMS, "UTF-8"));
                CloseableHttpResponse response = httpclient.execute(httppost);
                try {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String resultJson = EntityUtils.toString(entity, "UTF-8");
                        Map<String, String> resultMap = JsonUtils.objectFromJson(resultJson,
                            new TypeReference<Map<String, String>>() {
                            });
                        pushSuccess = "success".equals(resultMap.get("msg")) ? true : false;
                    }
                } catch (Exception e) {
                    pushSuccess = false;
                    LOGGER.error("解析httpPost response失败!", e);
                } finally {
                    response.close();
                }
            } catch (ClientProtocolException e) {
                LOGGER.error("ClientProtocolException!", e);
                pushSuccess = false;
            } catch (UnsupportedEncodingException e1) {
                LOGGER.error("UnsupportedEncodingException!", e1);
                pushSuccess = false;
            } catch (IOException e) {
                LOGGER.error("IOException!", e);
                pushSuccess = false;
            } finally {
                // 关闭连接,释放资源    
                httppost.releaseConnection();
            }
            if (!pushSuccess) {
                pushResponse = new HttpPushResponse();
                pushResponse.setRet(false);
                pushResponse.setAddress(address);
                responses.add(pushResponse);
            }
        }
        //返回所有失败的记录
        return responses;
    }

    /**
     * 向其它服务器发起探测
     * 
     * @param addresses
     * @return
     */
    public static List<HttpPushResponse> postDetect(List<String> addresses) {
        HttpPost httppost = null;
        List<HttpPushResponse> responses = new ArrayList<HttpPushResponse>();
        HttpPushResponse pushResponse = null;

        for (String address : addresses) {
            boolean pushSuccess = true;
            // 创建httppost    
            httppost = new HttpPost(HTTP_PREFIX + address + projectContextName + "/aliveDetect");
            //设置超时
            httppost.setConfig(defaultRequestConfig);
            try {
                CloseableHttpResponse response = httpclient.execute(httppost);
                try {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String resultJson = EntityUtils.toString(entity, "UTF-8");
                        Map<String, String> resultMap = JsonUtils.objectFromJson(resultJson,
                            new TypeReference<Map<String, String>>() {
                            });
                        pushSuccess = "success".equals(resultMap.get("msg")) ? true : false;
                    }
                } catch (Exception e) {
                    pushSuccess = false;
                    LOGGER.error("解析httpPost response失败!", e);
                } finally {
                    response.close();
                }
            } catch (ClientProtocolException e) {
                LOGGER.error("ClientProtocolException!", e);
                pushSuccess = false;
            } catch (UnsupportedEncodingException e1) {
                LOGGER.error("UnsupportedEncodingException!", e1);
                pushSuccess = false;
            } catch (IOException e) {
                LOGGER.error("IOException!", e);
                pushSuccess = false;
            } finally {
                // 关闭连接,释放资源    
                httppost.releaseConnection();
            }
            pushResponse = new HttpPushResponse();
            pushResponse.setRet(pushSuccess);
            pushResponse.setAddress(address);
            responses.add(pushResponse);
        }
        //返回所有失败的记录
        return responses;
    }
}
