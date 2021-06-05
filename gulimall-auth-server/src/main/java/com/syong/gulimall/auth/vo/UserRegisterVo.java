package com.syong.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Description: 使用JSR303进行校验
 */
@Data
public class UserRegisterVo {
    /**
     * 用户名
     **/
    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6,max = 18,message = "用户名必须是6-18位字符")
    private String username;

    /**
     * 密码
     **/
    @NotEmpty(message = "密码必须提交")
    @Length(min = 6,max = 18,message = "密码必须时6-18位字符")
    private String password;

    /**
     * 手机号码
     **/
    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "手机号格式不正确")
    private String mobile;

    /**
     * 验证码
     **/
    @NotEmpty(message = "验证码必须填写")
    private String code;
}
