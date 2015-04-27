package com.github.diamond.web.model;

import java.util.Date;

/**
 * 用于展示和推送
 * 
 * @author Administrator
 * @version $Id: ShowPushData.java, v 0.1 2015年2月2日 下午4:51:31 Administrator Exp
 *          $
 */
public class ShowPushData {
	private String projectCode;
	private String profileName;
	private String moduleNames;
	private String clientAddress;
	private Integer ctxIdentifier;
	private Date connTime;
	private Date lastConnTime;
	private String serverUUID;
	private String needPush;
	private String clientType;

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getModuleNames() {
		return moduleNames;
	}

	public void setModuleNames(String moduleNames) {
		this.moduleNames = moduleNames;
	}

	public String getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress = clientAddress;
	}

	public Date getConnTime() {
		return connTime;
	}

	public void setConnTime(Date connTime) {
		this.connTime = connTime;
	}

	public String getServerUUID() {
		return serverUUID;
	}

	public void setServerUUID(String serverUUID) {
		this.serverUUID = serverUUID;
	}

	public String getNeedPush() {
		return needPush;
	}

	public void setNeedPush(String needPush) {
		this.needPush = needPush;
	}

	public Integer getCtxIdentifier() {
		return ctxIdentifier;
	}

	public void setCtxIdentifier(Integer ctxIdentifier) {
		this.ctxIdentifier = ctxIdentifier;
	}

	/**
	 * Getter method for property <tt>lastConnTime</tt>.
	 * 
	 * @return property value of lastConnTime
	 */
	public Date getLastConnTime() {
		return lastConnTime;
	}

	/**
	 * Setter method for property <tt>lastConnTime</tt>.
	 * 
	 * @param lastConnTime
	 *            value to be assigned to property lastConnTime
	 */
	public void setLastConnTime(Date lastConnTime) {
		this.lastConnTime = lastConnTime;
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

}
