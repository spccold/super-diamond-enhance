package com.github.diamond.client.extend.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
/**
 * 
 * 
 * @author kangaungwen
 * @version $Id: HttpUtils.java, v 0.1 2015年2月4日 下午2:31:01 kanguangwen Exp $
 */
public class HttpUtils {
    /**初始化日志*/
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    //各种超时设置
    private static final RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setSocketTimeout(2000)
            .setConnectTimeout(2000)
            .setConnectionRequestTimeout(2000)
            .setStaleConnectionCheckEnabled(true)
            .build();
    
    // 创建默认的httpClient实例.    
    private static final CloseableHttpClient httpclient = HttpClients.custom()
            .setDefaultRequestConfig(defaultRequestConfig)
            .build();
    
    public static String post(List<NameValuePair> nameValuePairs,String clientAddress){
        // 创建httppost    
        HttpPost httppost = new HttpPost(clientAddress);
        //设置超时
        httppost.setConfig(defaultRequestConfig);
        try {
        	if(!CollectionUtils.isEmpty(nameValuePairs)){
        		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        	}
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity, "UTF-8");
            }
        
        } catch (ClientProtocolException e) {
            LOGGER.error("ClientProtocolException!",e);
        } catch (UnsupportedEncodingException e1) {
            LOGGER.error("UnsupportedEncodingException!",e1);
        } catch (IOException e) {
            LOGGER.error("IOException!",e);
        } finally {
            // 关闭连接,释放资源    
            httppost.releaseConnection();
        }
        return null;
    }
}
