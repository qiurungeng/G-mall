package com.atguigu.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截代码:

        //判断被拦截请求所访问的方法的注解（是否是需要拦截的）
        HandlerMethod hm=(HandlerMethod)handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        if (methodAnnotation==null){
            return true;
        }

        String token="";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)){
            token=oldToken;
        }
        String newToekn = request.getParameter("token");
        if (StringUtils.isNotBlank(newToekn)){
            token=newToekn;
        }

        //进入拦截器的拦截方法
        boolean loginSuccessNeeded = methodAnnotation.loginSuccessNeeded(); //该请求是否必须成功登录



        String verify = "";
        Map verificationMap=null;
        //获得发起请求的客户端的ip
        String ip=request.getHeader("x-forwarded-for");     //通过nginx转发的客户端ip
        if (StringUtils.isBlank(ip)){
            ip=request.getRemoteAddr();     //从request中获取ip
            if (StringUtils.isBlank(ip)){
                ip="123.123.123.123";       //异常情况，不做过多拓展
            }
        }
        //调用认证中心进行验证
        if (StringUtils.isNotBlank(token)){
            //请求认证中心进行验证，得到验证结果verificationMap
            String verifyJson=HttpclientUtil
                    .doGet("http://passport.gmall.com:8085/verify?token=" + token+"&currentIp="+ip);
            verificationMap= JSON.parseObject(verifyJson,Map.class);
            assert verificationMap != null;
            verify= (String) verificationMap.get("status");
        }

        if (loginSuccessNeeded){
            //必须登录成功才能放行
            if (!verify.equals("success")){
                //从未登录，踢回认证中心
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="
                        +request.getRequestURL()+"&requestIP="+ip);
                return false;
            }else {
                //验证通过，覆盖cookie中的token
                //已登录，需要将token携带的用户信息写入
                request.setAttribute("memberId",verificationMap.get("memberId"));
                request.setAttribute("nicknam",verificationMap.get("nickname"));
                //验证通过，覆盖cookie中的token
                if (StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }
            }
        }else {
            //没登录也能用，但必须验证
            if (verify.equals("success")){
                //已登录，需要将token携带的用户信息写入
                request.setAttribute("memberId",verificationMap.get("memberId"));
                request.setAttribute("nicknam",verificationMap.get("nickname"));
                //验证通过，覆盖cookie中的token

                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);

            }
        }

        return true;
    }


}
