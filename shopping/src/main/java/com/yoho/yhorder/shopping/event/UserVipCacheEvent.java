package com.yoho.yhorder.shopping.event;

import com.yoho.yhorder.shopping.model.UserInfo;

/**
 * Created by wujiexiang on 16/6/29.
 */
public class UserVipCacheEvent {

    private UserInfo userInfo;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
