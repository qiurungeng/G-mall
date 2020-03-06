package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.mq.ActiveMQUtil;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }


    @Override
    public void payUp(PaymentInfo paymentInfo) {
        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());
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
                if (session!=null)session.rollback();
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
