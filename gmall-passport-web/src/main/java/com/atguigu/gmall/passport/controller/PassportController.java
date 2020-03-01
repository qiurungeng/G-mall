package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.HttpclientUtil;
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

    /**
     * 本平台账号登录
     * @param umsMember 登录时输入的账号信息
     * @return  token
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        //调用用户服务验证用户名和密码进行登录
        UmsMember loginUser=userService.login(umsMember);
        //由登录结果返回token
        return makeToken(loginUser,request);
    }

    /**
     * 通过微博授权登录
     * @param code 微博平台授权码
     * @return  token
     */
    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request){
        //授权码换取access_token
        String access_token_url="https://api.weibo.com/oauth2/access_token?";
        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("client_id","547721421");
        paramMap.put("client_secret","7f2d0d5edb88f273e22b6deac935d312");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("code",code);
        String access_token_json = HttpclientUtil.doPost(access_token_url, paramMap);
        Map access_token_map=JSON.parseObject(access_token_json,Map.class);
        //access_token换取用户信息
        assert access_token_map != null;
        Long uid = Long.parseLong((String) access_token_map.get("uid"));
        String access_token = (String) access_token_map.get("access_token");
        String query_user_url="https://api.weibo.com/2/users/show.json?access_token="
                +access_token+"&uid="+uid;
        String user_json=HttpclientUtil.doGet(query_user_url);
        Map user_map = JSON.parseObject(user_json, Map.class);
        //将用户信息保存到数据库，用户类型设置为微博用户
        assert user_map!=null;
        UmsMember umsMember=new UmsMember();
        umsMember.setSourceType(2);
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid(uid);
        umsMember.setNickname((String) user_map.get("screen_name"));
        umsMember.setCity((String)user_map.get("location"));
        umsMember.setGender(user_map.get("gender").equals("m")?(user_map.get("gender").equals("f")?2:1):0);
        UmsMember login = userService.loginOauthUser(umsMember);

        //生成jwt的token，并且重定向到首页，携带该token
        String token=makeToken(login,request);
        return "redirect:http://search.gmall.com:8083/?token="+token;
    }

    /**
     * 为登录用户制作token
     * @param umsMember 登录用户信息
     * @return token
     */
    private String makeToken(UmsMember umsMember,HttpServletRequest request){
        String token="";
        if (umsMember!=null){
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
