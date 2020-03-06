package com.atguigu.gmall.payment;

import com.atguigu.gmall.mq.ActiveMQUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallPaymentApplicationTests {
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Test
    public void contextLoads() throws JMSException {
        ConnectionFactory connectionFactory=activeMQUtil.getConnectionFactory();
        Connection connection = connectionFactory.createConnection();
        System.out.println(connection);
    }

}
