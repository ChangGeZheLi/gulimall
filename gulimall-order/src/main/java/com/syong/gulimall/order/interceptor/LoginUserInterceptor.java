package com.syong.gulimall.order.interceptor;

import com.syong.common.constant.AuthServerConstant;
import com.syong.common.vo.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Description: 拦截用户是否登录
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberEntity> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        MemberEntity memberEntity = (MemberEntity) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberEntity != null){
            //登录了
            threadLocal.set(memberEntity);
            return true;
        }else {
            //没登陆
            session.setAttribute("msg","请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
