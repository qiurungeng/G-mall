package com.atguigu.gmall.user.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.user.mapper.UserReceiveAddressMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserReceiveAddressMapper userReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMemberList=userMapper.selectAll();
        return umsMemberList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
//        UmsMemberReceiveAddress umsMemberReceiveAddress=new UmsMemberReceiveAddress();
//        umsMemberReceiveAddress.setMemberId(memberId);
//
//        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userReceiveAddressMapper.select(umsMemberReceiveAddress);

        Example example=new Example(UmsMemberReceiveAddress.class);
        example.createCriteria().andEqualTo("memberId",memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userReceiveAddressMapper.selectByExample(example);
        return umsMemberReceiveAddresses;
    }

    /**
     * 用户登录
     * @param umsMember 登录用户信息，包含用户名密码
     * @return 成功登录用户的所有信息
     */
    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis=null;
        try {
            jedis=redisUtil.getJedis();
            if (jedis!=null){
                String userInfoStr = jedis.get("user:" + umsMember.getUsername()+umsMember.getPassword() + ":info");
                if (StringUtils.isNotBlank(userInfoStr)){
                    //命中缓存，直接返回
                    return JSON.parseObject(userInfoStr, UmsMember.class);
                }
            }
            //密码错误或缓存中没有或redis宕机,查数据库
            UmsMember loginUser=loginFromDb(umsMember);
            if (loginUser!=null&&jedis!=null){
                jedis.setex("user:" + loginUser.getUsername()+loginUser.getPassword() + ":info",
                        60*60*24,JSON.toJSONString(loginUser));
            }
            return loginUser;
        }finally {
            assert jedis != null;
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+memberId+":token",60*60*2,token);
        jedis.close();
    }

    /**
     * 添加社交登录用户信息
     * @param umsMember 社交登录用户
     */
    @Override
    public UmsMember loginOauthUser(UmsMember umsMember) {
        UmsMember check=new UmsMember();
        check.setSourceUid(umsMember.getSourceUid());
        check.setSourceType(umsMember.getSourceType());
        UmsMember exist = userMapper.selectOne(check);
        if (exist==null){
            //首次采用第三方平台账号登录
            userMapper.insertSelective(umsMember);
        }else {
            //已有该第三方账号记录
            Example example=new Example(UmsMember.class);
            example.createCriteria().andEqualTo("sourceUid",umsMember.getSourceUid())
                    .andEqualTo("sourceType",umsMember.getSourceType());
            userMapper.updateByExampleSelective(umsMember,example);     //更新一下账号信息
        }
        return userMapper.selectOne(umsMember);     //返回完整的登录用户信息
    }

    /**
     * 通过数据库查找的方式登录
     * @param umsMember 登录用户信息，包含用户名密码
     * @return 成功登录用户的所有信息
     */
    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> select = userMapper.select(umsMember);
        if (select!=null)return select.get(0);
        return null;
    }
}
