package com.syong.gulimall.cart.to;

import lombok.Data;

/**
 * @Description: 封装传递到页面的用户信息
 */
@Data
public class UserInfoTo {

    /**
     * 登录后的用户id
     **/
    private Long userId;
    /**
     * 没登陆则有user-key
     **/
    private String userKey;
    /**
     * 是否有临时用户
     **/
    private Boolean tempUser = false;
}
