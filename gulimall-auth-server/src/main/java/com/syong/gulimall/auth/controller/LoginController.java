package com.syong.gulimall.auth.controller;

import com.syong.common.utils.R;
import com.syong.gulimall.auth.service.LoginService;
import com.syong.gulimall.auth.vo.UserLoginVo;
import com.syong.gulimall.auth.vo.UserRegisterVo;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 */
@Controller
public class LoginController {

    @Resource
    private LoginService loginService;

    /**
     * 验证码发送
     **/
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("mobile") String mobile){

        R r = loginService.sendCode(mobile);

        return r;
    }

    /**
     * 用户注册
     **/
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()){
            //如果校验不成功
            //封装错误数据给前端
            Map<String, String> collect = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
//            model.addAttribute("errors",collect);

            //防止表单重复提交，使用重定向
            redirectAttributes.addFlashAttribute("errors",collect);

            return "redirect:http://auth.gulimall.com/register.html";
        }

        //如果没有错误，则将数据存入数据库，需要远程调用会员服务
        //校验验证码是否正确
        Map<String, String> register = loginService.register(vo);
        //验证码不匹配
        if (register.containsKey("code")){
            redirectAttributes.addFlashAttribute("errors",register);
            return "redirect:http://auth.gulimall.com/register.html";
        }else if (register.containsKey("msg")){
            //注册不成功
            redirectAttributes.addFlashAttribute("errors", register);
            return "redirect:http://auth.gulimall.com/register.html";
        }else if (register.containsKey("success")){
            //注册成功，重定向到登录页
            return "redirect:http://auth.gulimall.com/login.html";
        }else {
            return "redirect:http://auth.gulimall.com/register.html";
        }
    }

    /**
     * 登录
     **/
    @PostMapping("/login")
    public String login(UserLoginVo vo,RedirectAttributes redirectAttributes){

        Map<String, String> map = loginService.login(vo);

        if (map.containsKey("success")){
            return "redirect:http://gulimall.com";
        }else {
            redirectAttributes.addFlashAttribute("errors",map);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
