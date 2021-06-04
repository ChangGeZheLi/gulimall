package com.syong.gulimall.auth.service;

import com.syong.common.utils.R;

/**
 * @Description:
 */
public interface LoginService {

    R sendCode(String mobile);
}
