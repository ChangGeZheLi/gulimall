package com.syong.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.syong.common.exception.BizCodeEnum;
import com.syong.gulimall.member.exception.MobileExistException;
import com.syong.gulimall.member.exception.UsernameExistException;
import com.syong.gulimall.member.feign.CouponFeignService;
import com.syong.gulimall.member.vo.UserLoginVo;
import com.syong.gulimall.member.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.syong.gulimall.member.entity.MemberEntity;
import com.syong.gulimall.member.service.MemberService;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.R;



/**
 * 会员
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:18:36
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    /**
     * 会员登录
     **/
    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo vo){
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity!=null){
            return R.ok();
        }else {
            return R.error(BizCodeEnum.LOGINUSER_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINUSER_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    /**
     * 会员注册功能
     **/
    @PostMapping("/register")
    public R register(@RequestBody UserRegisterVo vo){

        try{
            memberService.register(vo);
        }catch (MobileExistException e){
            return R.error(BizCodeEnum.MOBILE_EXIST_EXCEPTION.getCode(),BizCodeEnum.MOBILE_EXIST_EXCEPTION.getMsg());
        }catch (UsernameExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 测试openFeign调用
     **/
    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        R coupons = couponFeignService.memberCoupons();
        return R.ok().put("memebre",memberEntity).put("coupons",coupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
