package com.syong.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.member.entity.MemberEntity;
import com.syong.gulimall.member.exception.MobileExistException;
import com.syong.gulimall.member.exception.UsernameExistException;
import com.syong.gulimall.member.vo.SocialUser;
import com.syong.gulimall.member.vo.UserLoginVo;
import com.syong.gulimall.member.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:18:36
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo vo);

    void checkMobileUnique(String mobile) throws MobileExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(UserLoginVo vo);

    MemberEntity oauthLogin(SocialUser user);
}

