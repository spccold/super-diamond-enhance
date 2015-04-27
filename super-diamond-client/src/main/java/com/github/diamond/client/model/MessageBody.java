package com.github.diamond.client.model;


public class MessageBody {
    private String projCode;
    private String profile;
    private String modules;
    private String version;
    private String clientAddress;
    
    public String getProjCode() {
        return projCode;
    }
    public void setProjCode(String projCode) {
        this.projCode = projCode;
    }
    public String getProfile() {
        return profile;
    }
    public void setProfile(String profile) {
        this.profile = profile;
    }
    public String getModules() {
        return modules;
    }
    public void setModules(String modules) {
        this.modules = modules;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getClientAddress() {
        return clientAddress;
    }
    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }
}
