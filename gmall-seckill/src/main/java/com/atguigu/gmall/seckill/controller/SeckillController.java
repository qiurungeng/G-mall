package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SeckillController {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("/seckill")
    @ResponseBody
    public String seckill(){
        Jedis jedis = redisUtil.getJedis();
        RSemaphore semaphore = redissonClient.getSemaphore("122");
        boolean b = semaphore.tryAcquire();
        if (b){
            int stock = Integer.parseInt(jedis.get("122"));
            System.out.println("当前库存剩余数量："+stock+
                    ",某某用户抢购成功。当前抢购人数："+(1000-stock));
            //消息队列发出订单消息
        }else {
            int stock = Integer.parseInt(jedis.get("122"));
            System.out.println("当前库存剩余数量："+stock+
                    ",某某用户抢购失败");
        }
        jedis.close();
        return "1";
    }

    @RequestMapping("/kill")
    @ResponseBody
    public String index(){
        String memberId="1";
        Jedis jedis=redisUtil.getJedis();
        jedis.watch("122");
        int stock=Integer.parseInt(jedis.get("122"));
        if (stock>0){
            Transaction multi = jedis.multi();
            multi.incrBy("122",-1);
            List<Object> exec = multi.exec();
            if (exec!=null&&exec.size()>0){
                System.out.println("当前库存剩余数量："+stock+
                        ",某某用户抢购成功。当前抢购人数："+(1000-stock));
                //消息队列发出订单消息
            }else {
                System.out.println("当前库存剩余数量："+stock+
                        ",某某用户抢购失败");
            }
        }
        jedis.close();
        return "1";
    }
}
