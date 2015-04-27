package com.github.diamond.client.extend.api.model;

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
    private String connTime;
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
}
