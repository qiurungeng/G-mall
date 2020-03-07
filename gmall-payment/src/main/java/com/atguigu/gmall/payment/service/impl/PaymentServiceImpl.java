package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.mq.ActiveMQUtil;
import com.atguigu.gmall.mq.MyMsgHandler;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;


@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    /**
     * 完成付款，保存付款信息
     * @param paymentInfo 付款信息
     */
    @Override
    public void payUp(PaymentInfo paymentInfo) {
        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());
        //进行幂等性检查,若已支付则不进行任何操作
        PaymentInfo check = paymentInfoMapper.selectOneByExample(example);
        if (StringUtils.isNotBlank(check.getPaymentStatus())&&check.getPaymentStatus().equals("已支付")){
            return;
        }
        //支付成功后的系统服务->订单服务->库存服务->物流
        //调用mq发送支付成功消息
        Connection connection=null;
        Session session=null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);//可回滚
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
            assert session != null;
            Queue queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            MapMessage mapMessage=new ActiveMQMapMessage(); //hash结构
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
            producer.send(mapMessage);
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

    /**
     * 发送延迟消息队列，检查是否已成功支付
     * @param outTradeNo 订单号
     * @param count 剩余检查次数
     */
    @Override
    public void sendDelayPaymentResultCheckQueue(String outTradeNo,int count) {
        String queueName="PAYMENT_CHECK_QUEUE";
        MapMessage mapMessage=new ActiveMQMapMessage();
        try {
            mapMessage.setString("out_trade_no",outTradeNo);
            mapMessage.setInt("count",count);
            //为消息加入延迟时间30s
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);
            sendQueueMsg(queueName,mapMessage);    //只发送消息，不执行普通业务
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用支付宝API查询订单支付情况
     * @param out_trade_no 订单号
     * @return 支付情况查询结果
     */
    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {
        Map<String,Object> resultMap=new HashMap<>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("out_trade_no",out_trade_no);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用支付宝服务接口查询交易结果，交易可能创建成功");
            resultMap.put("out_trade_no",response.getOutTradeNo());
            resultMap.put("trade_no",response.getTradeNo());
            resultMap.put("trade_status",response.getTradeStatus());
            resultMap.put("call_back_content",response.getMsg());
        } else {
            System.out.println("调用支付宝服务接口查询交易结果，交易可能失败");
        }
        return resultMap;
    }

    /**
     * 发送简单消息
     * @param queueName 消息队列名称
     * @param message 消息内容
     */
    private void sendQueueMsg(String queueName, Message message){
        Connection connection=null;
        Session session=null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);//可回滚
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            assert session != null;
            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);
            producer.send(message);
            session.commit();
        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (connection!=null)connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }


}
