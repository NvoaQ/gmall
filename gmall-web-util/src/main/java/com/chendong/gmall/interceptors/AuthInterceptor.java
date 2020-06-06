package com.chendong.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.chendong.gmall.annotations.LoginRequire;
import com.chendong.gmall.util.CookieUtil;
import com.chendong.gmall.utils.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    /**
     * 除了可以通过web模块是否扫描拦截器来决定拦截器的使用之外，
     * 还可以通过注解的方式来标识具体的方法是否需要通过拦截器
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截代码
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获取自定义的拦截注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //不需要拦截，直接放行
        if (methodAnnotation == null) {
            return true;
        }
        //获取token
        String token = "";
        //从cookie里取token
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        //是否必须登录
        boolean loginSuccess = methodAnnotation.loginSuccess();
        //请求ip
        String remoteAddr = request.getRemoteAddr();
        if (StringUtils.isBlank(remoteAddr)) {
            remoteAddr = "127.0.0.1";
        }
        String success = "fail";
        Map<String, String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)) {
            //去认证中心，验证token
            String successJSON = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token + "&remoteAddr=" + remoteAddr);
            successMap = JSON.parseObject(successJSON, Map.class);
            success = successMap.get("status");
        }

        //强制需要身份验证，必须登录
        if (loginSuccess) {
            if (!success.equals("success")) {
                //重定向到认证中心
                StringBuffer originUrl = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?originUrl=" + originUrl);
                return false;
            } else if (success.equals("success")) {
                //向请求存入用户id和用户昵称
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("memberNickname", successMap.get("memberNickname"));
                if (StringUtils.isNotBlank(token)) {
                    //覆盖cookie的token
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                }
            }
        } else {
            //不强制需要身份验证，但也验证，验证通过重置cookie放行，不通过也放行
            //验证token
            if (success.equals("success")) {
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("memberNickname", successMap.get("memberNickname"));
                if (StringUtils.isNotBlank(token)) {
                    //覆盖cookie的token
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                }
                return true;
            }
        }
        return true;
    }
}
