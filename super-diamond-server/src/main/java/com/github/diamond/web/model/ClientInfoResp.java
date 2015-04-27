package com.github.diamond.web.model;

/**
 * 
 * 
 * @author Administrator
 * @version $Id: ClientInfoResp.java, v 0.1 2015年2月3日 下午1:26:41 Administrator Exp $
 */
public class ClientInfoResp {
    /**客户端地址*/
    private String address;
    private String projectName;
    /**客户端连接时间的字符串标示，用于页面展示*/
    private String connTime;
    private String lastConnTime;
    private String clientType;
    
    public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getConnTime() {
        return connTime;
    }

    public void setConnTime(String connTime) {
        this.connTime = connTime;
    }

    public String getLastConnTime() {
        return lastConnTime;
    }

    public void setLastConnTime(String lastConnTime) {
        this.lastConnTime = lastConnTime;
    }
}
