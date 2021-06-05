package com.syong.gulimall.auth.service;

import com.syong.common.utils.R;
import com.syong.gulimall.auth.vo.UserLoginVo;
import com.syong.gulimall.auth.vo.UserRegisterVo;

import java.util.Map;

/**
 * @Description:
 */
public interface LoginService {

    R sendCode(String mobile);

    Map<String, String> register(UserRegisterVo vo);

    Map<String, String> login(UserLoginVo vo);
}
