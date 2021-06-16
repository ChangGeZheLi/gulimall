package com.syong.common.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员
 * 
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:18:36
 */
@Data
@ToString
public class 	MemberEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/**
	 * 会员等级id
	 */
	private Long levelId;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 手机号码
	 */
	private String mobile;
	/**
	 * 邮箱
	 */
	private String email;
	/**
	 * 头像
	 */
	private String header;
	/**
	 * 性别
	 */
	private Integer gender;
	/**
	 * 生日
	 */
	private Date birth;
	/**
	 * 所在城市
	 */
	private String city;
	/**
	 * 职业
	 */
	private String job;
	/**
	 * 个性签名
	 */
	private String sign;
	/**
	 * 用户来源
	 */
	private Integer sourceType;
	/**
	 * 积分
	 */
	private Integer integration;
	/**
	 * 成长值
	 */
	private Integer growth;
	/**
	 * 启用状态
	 */
	private Integer status;
	/**
	 * 注册时间
	 */
	private Date createTime;
	/**
	 * 社交账号ID
	 */
	private Long socialUid;
	/**
	 * 社交账号Token
	 */
	private String accessToken;
	/**
	 * 社交账号Token过期时间
	 */
	private Long expiresIn;
}
