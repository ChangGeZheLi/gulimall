package com.syong.gulimall.member.interceptor;

import com.syong.common.constant.AuthServerConstant;
import com.syong.common.vo.MemberEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器，未登录的用户不能进入订单服务
 */
public class LoginInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberEntity> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //放行无需进行登录的请求
        String requestURI = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean match = matcher.match("/member/**", requestURI);
        if (match){
            return true;
        }

        HttpSession session = request.getSession();
        MemberEntity memberResponseVo = (MemberEntity) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberResponseVo != null) {
            threadLocal.set(memberResponseVo);
            return true;
        }else {
            session.setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
