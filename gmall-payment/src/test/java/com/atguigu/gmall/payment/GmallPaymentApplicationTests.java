package com.atguigu.gmall.payment;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.mq.ActiveMQUtil;
import com.atguigu.gmall.mq.MyMsgHandler;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.mapper.entity.Example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.lang.reflect.Method;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallPaymentApplicationTests {
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Reference
    PaymentInfoMapper paymentInfoMapper;

    @Test
    public void contextLoads() throws JMSException {
        ConnectionFactory connectionFactory=activeMQUtil.getConnectionFactory();
        Connection connection = connectionFactory.createConnection();
        System.out.println(connection);
    }

//    @Test
//    public void testReflection(){
//        MapMessage mapMessage=new ActiveMQMapMessage();
//        try {
//            mapMessage.setString("out_trade_no","111");
//            MyMsgHandler.sendQueueMsg("TEST",mapMessage);
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }
//    }

}
