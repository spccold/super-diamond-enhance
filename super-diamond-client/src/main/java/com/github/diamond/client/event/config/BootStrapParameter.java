package com.github.diamond.client.event.config;

/**
 * 应用系统启动参数
 * 
 * @author 机冷
 * @version $Id: BootStrapParameter.java, v 0.1 2015年2月28日 上午11:21:28 kanguangwen Exp $
 */
public class BootStrapParameter {
    /**super-diamond-server netty host*/
    private String diamondHost;
    /**netty 端口*/
    private int    diamondPort;
    /**项目编码*/
    private String diamondProjcode;
    /**环境(development,build,test,production)*/
    private String diamondProfile;

    /**配置模块*/
    private String diamondModules;

    /**
     * Getter method for property <tt>diamondHost</tt>.
     * 
     * @return property value of diamondHost
     */
    public String getDiamondHost() {
        return diamondHost;
    }

    /**
     * Setter method for property <tt>diamondHost</tt>.
     * 
     * @param diamondHost value to be assigned to property diamondHost
     */
    public void setDiamondHost(String diamondHost) {
        this.diamondHost = diamondHost;
    }

    /**
     * Getter method for property <tt>diamondPort</tt>.
     * 
     * @return property value of diamondPort
     */
    public int getDiamondPort() {
        return diamondPort;
    }

    /**
     * Setter method for property <tt>diamondPort</tt>.
     * 
     * @param diamondPort value to be assigned to property diamondPort
     */
    public void setDiamondPort(int diamondPort) {
        this.diamondPort = diamondPort;
    }

    /**
     * Getter method for property <tt>diamondProjcode</tt>.
     * 
     * @return property value of diamondProjcode
     */
    public String getDiamondProjcode() {
        return diamondProjcode;
    }

    /**
     * Setter method for property <tt>diamondProjcode</tt>.
     * 
     * @param diamondProjcode value to be assigned to property diamondProjcode
     */
    public void setDiamondProjcode(String diamondProjcode) {
        this.diamondProjcode = diamondProjcode;
    }

    /**
     * Getter method for property <tt>diamondProfile</tt>.
     * 
     * @return property value of diamondProfile
     */
    public String getDiamondProfile() {
        return diamondProfile;
    }

    /**
     * Setter method for property <tt>diamondProfile</tt>.
     * 
     * @param diamondProfile value to be assigned to property diamondProfile
     */
    public void setDiamondProfile(String diamondProfile) {
        this.diamondProfile = diamondProfile;
    }

    /**
     * Getter method for property <tt>diamondModules</tt>.
     * 
     * @return property value of diamondModules
     */
    public String getDiamondModules() {
        return diamondModules;
    }

    /**
     * Setter method for property <tt>diamondModules</tt>.
     * 
     * @param diamondModules value to be assigned to property diamondModules
     */
    public void setDiamondModules(String diamondModules) {
        this.diamondModules = diamondModules;
    }
}
