package com.github.diamond.web.model;

public class HttpPushResponse {
    /**是否成功*/
    private boolean ret;
    /**对应的服务器地址*/
    private String address;

    public boolean isRet() {
        return ret;
    }
    public void setRet(boolean ret) {
        this.ret = ret;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
