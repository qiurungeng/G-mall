package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){
        modelMap.put("ReturnUrl",ReturnUrl);
        return "index";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String currentIp){
        //通过JWT校验token真假

        Map<String,String> map=new HashMap<>();
        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall", currentIp);
        if (decode!=null){
            map.put("status","success");
            map.put("memberId",(String) decode.get("memberId"));
            map.put("nickname",(String) decode.get("nickname"));
        }else {
            map.put("status","fail");
        }
        return JSON.toJSONString(map);
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        String token="";
        //调用用户服务验证用户名和密码
        UmsMember loginUser=userService.login(umsMember);
        if (loginUser!=null){
            //登录成功，用JWT制作token
            Map<String,Object> userMap=new HashMap<>();
            userMap.put("memberId",umsMember.getId());
            userMap.put("nickname",umsMember.getNickname());

            String ip=request.getHeader("x-forwarded-for");     //通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)){
                ip=request.getRemoteAddr();     //从request中获取ip
                if (StringUtils.isBlank(ip)){
                    ip="123.123.123.123";       //异常情况，不做过多拓展
                }
            }
            token = JwtUtil.encode("2019gmall", userMap, ip);
            //存入一份token到redis
            userService.addUserToken(token,umsMember.getId());
        }else {
            //登录失败
            token+="FAILED";
        }
        return token;
    }
}
