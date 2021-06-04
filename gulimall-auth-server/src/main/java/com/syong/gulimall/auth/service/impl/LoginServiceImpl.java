package com.syong.gulimall.auth.service.impl;

import com.syong.common.constant.AuthServerConstant;
import com.syong.common.exception.BizCodeEnum;
import com.syong.common.utils.R;
import com.syong.gulimall.auth.feign.ThirdPartyFeign;
import com.syong.gulimall.auth.service.LoginService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private ThirdPartyFeign thirdPartyFeign;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R sendCode(String mobile) {

        //防止恶意短信发送接口调用
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + mobile);
        if (!StringUtils.isEmpty(redisCode)){
            Long l  = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l > 60000){
                //60s内不能在发验证码
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = UUID.randomUUID().toString().substring(0,5)+"_"+System.currentTimeMillis();

        //验证码的再次校验，使用redis缓存实现
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+mobile,code,5, TimeUnit.MINUTES);

        thirdPartyFeign.sendCode(mobile,code);

        return R.ok();
    }
}
