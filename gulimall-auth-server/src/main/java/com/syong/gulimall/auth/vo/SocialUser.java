package com.syong.gulimall.auth.vo;


import lombok.Data;

import java.util.Date;

/**
 * @Description: 对应gitee使用授权码获取access_token返回的json
 */
@Data
public class SocialUser {

    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;
    private Long uid;
    private String name;
    private Date createTime;
}
