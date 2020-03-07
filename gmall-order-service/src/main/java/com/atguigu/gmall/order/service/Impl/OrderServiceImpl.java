package com.atguigu.gmall.order.service.Impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.mq.ActiveMQUtil;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Reference
    CartService cartService;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String checkTradeCode(String memberId,String tradeCode) {
        Jedis jedis=null;
        try{
            jedis=redisUtil.getJedis();
            String tradeKey="user:" + memberId + ":tradeCode";
            //使用lua脚本在发现key的同时删除key，防止订单攻击
            String script="if redis.call('get',KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del',KEYS[1]) else return 0 end";
            Long eval=(Long) jedis.eval(script, Collections.singletonList(tradeKey),
                    Collections.singletonList(tradeCode));
            if (eval!=null&&eval!=0){
                return "success";
            }else return "fail";
        }finally {
            assert jedis != null;
            jedis.close();
        }
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis=redisUtil.getJedis();
        String tradeKey="user:"+memberId+":tradeCode";
        String tradeCode= UUID.randomUUID().toString();
        jedis.setex(tradeKey,60*15,tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        //保存订单详情
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车数据
            cartService.delCart(omsOrder.getMemberId());
        }

    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder=new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        return omsOrderMapper.selectOne(omsOrder);
    }

    @Override
    public void orderPayUp(OmsOrder omsOrder) {
        OmsOrder payUpOrder=new OmsOrder();
        payUpOrder.setStatus(1);  //已付款，待发货
        payUpOrder.setPaymentTime(new Date());
        Example example=new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());


        //发送一个订单已支付队列给库存消费
        //支付成功后的系统服务->订单服务->库存服务->物流
        Connection connection=null;
        Session session=null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);//可回滚
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            omsOrderMapper.updateByExampleSelective(payUpOrder,example);
            assert session != null;
            Queue queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(queue);
//            MapMessage mapMessage=new ActiveMQMapMessage(); //hash结构
//            mapMessage.setString("out_trade_no",omsOrder.getOrderSn());
            TextMessage textMessage=new ActiveMQTextMessage();

            //查询订单及订单对应的订单商品，转换为json字符串，存入ORDER_PAY_QUEUE消息队列
            OmsOrder selectOrderParam=new OmsOrder();
            selectOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResponse = omsOrderMapper.selectOne(selectOrderParam);
            OmsOrderItem selectOrderItemParam=new OmsOrderItem();
            selectOrderItemParam.setOrderSn(omsOrderResponse.getOrderSn());
            List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(selectOrderItemParam);
            omsOrderResponse.setOmsOrderItems(omsOrderItems);
            textMessage.setText(JSON.toJSONString(omsOrderResponse));
            //发送订单消息
            producer.send(textMessage);
            session.commit();
        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                if (connection!=null)connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

}
