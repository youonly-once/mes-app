package com.shu.messystem.result_bean;

/**
 * Created by Administrator on 2018/4/20.
 */

public class GetUserInfoBean {


    /**
     * username : 01405645
     * upword : 123
     * realname :
     * msg : success
     */
//登录所需信息
    private String username;
    private String upword;
    private String realname;
    private String msg;

//查询权限所需信息
    private String permission;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUpword() {
        return upword;
    }

    public void setUpword(String upword) {
        this.upword = upword;
    }

    public String getRealname() {
        return realname;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
