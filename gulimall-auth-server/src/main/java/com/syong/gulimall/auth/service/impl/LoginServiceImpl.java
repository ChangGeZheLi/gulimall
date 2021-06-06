package com.syong.gulimall.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.syong.common.constant.AuthServerConstant;
import com.syong.common.exception.BizCodeEnum;
import com.syong.common.utils.R;
import com.syong.common.vo.MemberEntity;
import com.syong.gulimall.auth.feign.MemberFeignService;
import com.syong.gulimall.auth.feign.ThirdPartyFeignService;
import com.syong.gulimall.auth.service.LoginService;
import com.syong.gulimall.auth.vo.UserLoginVo;
import com.syong.gulimall.auth.vo.UserRegisterVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private ThirdPartyFeignService thirdPartyFeignService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MemberFeignService memberFeignService;


    @Override
    public R sendCode(String mobile) {

        //防止恶意短信发送接口调用
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + mobile);
        if (!StringUtils.isEmpty(redisCode)){
            Long l  = Long.parseLong(redisCode.split("_")[1]);
            if ((System.currentTimeMillis() - l) < 60000){
                //60s内不能在发验证码
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = UUID.randomUUID().toString().substring(0,5)+"_"+System.currentTimeMillis();

        //验证码的再次校验，使用redis缓存实现,并设置过期时间
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+mobile,code,5, TimeUnit.MINUTES);

        String[] s = code.split("_");

        thirdPartyFeignService.sendCode(mobile,s[0]);

        return R.ok();
    }

    /**
     * 注册信息无误，则需要将数据写入数据库
     *
     * @param vo
     * @return*/
    @Override
    public Map<String, String> register(UserRegisterVo vo) {

        Map<String, String> map = new HashMap<>();

        //先校验验证码是否正确
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getMobile());
        String[] s = redisCode.split("_");

        if (!StringUtils.isEmpty(redisCode)){
            //判断验证码是否正确
            if (vo.getCode().equals(s[0])){
                //删除redis保存的验证码;令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getMobile());

                //校验成功.调用远程会员服务进行注册
                R r = memberFeignService.register(vo);
                if (r.getCode() == 0){
                    //注册成功
                    map.put("success","");
                    return map;
                }else {
                    //注册不成功
                    map.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    return map;
                }

            }else{
                //验证码不匹配
                map.put("code","验证码错误");
                return map;
            }
        }else {
            //redis 验证码为空
            map.put("error","验证码为空");
            return map;
        }
    }

    /**
     * 会员登录
     * @return
     */
    @Override
    public Map<String, String> login(UserLoginVo vo) {

        Map<String,String> map = new HashMap<>();

        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            MemberEntity memberEntity = r.getData("data", new TypeReference<MemberEntity>() {});

            String data = JSON.toJSONString(memberEntity);
            System.out.println("data:  " + data);
            map.put("success",data);

            return map;
        }else {
            map.put("msg",r.getData("msg",new TypeReference<String>(){}));
            return map;
        }

    }
}
