package com.syong.gulimall.member.service.impl;

import com.syong.gulimall.member.entity.MemberLevelEntity;
import com.syong.gulimall.member.exception.MobileExistException;
import com.syong.gulimall.member.exception.UsernameExistException;
import com.syong.gulimall.member.service.MemberLevelService;
import com.syong.gulimall.member.vo.UserLoginVo;
import com.syong.gulimall.member.vo.UserRegisterVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.member.dao.MemberDao;
import com.syong.gulimall.member.entity.MemberEntity;
import com.syong.gulimall.member.service.MemberService;

import javax.annotation.Resource;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 会员注册
     **/
    @Override
    public void register(UserRegisterVo vo) {

        MemberEntity memberEntity = new MemberEntity();

        //设置会员等级，需要查询数据库
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一
        //使用异常机制，让controller能够感知到是哪一个错误信息
        checkMobileUnique(vo.getMobile());
        checkUsernameUnique(vo.getUsername());

        memberEntity.setUsername(vo.getUsername());
        memberEntity.setMobile(vo.getMobile());

        //密码需要加密存储，使用spring封装的MD5加salt加密算法
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        //其他默认信息

        this.baseMapper.insert(memberEntity);

    }

    @Override
    public void checkMobileUnique(String mobile) throws MobileExistException {
        //判断数据库是否有该手机号
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", mobile));
        if (count > 0){
            throw new MobileExistException();
        }

    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        //判断数据库是否有该用户名
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0){
            throw new UsernameExistException();
        }
    }

    /**
     * 会员登录
     **/
    @Override
    public MemberEntity login(UserLoginVo vo) {

        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", vo.getLoginUser()).or().eq("mobile", vo.getLoginUser()));
        if (memberEntity==null){
            //登录失败
            return null;
        }else {
            //先将数据库中的密码查询出来，然后在进行对比
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(vo.getPassword(), memberEntity.getPassword());
            if (matches){
                //密码匹配成功
                return memberEntity;
            }else {
                return null;
            }
        }
    }

}