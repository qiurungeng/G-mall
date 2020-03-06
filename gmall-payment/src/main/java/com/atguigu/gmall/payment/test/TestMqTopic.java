package com.atguigu.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class TestMqTopic {
    public static void main(String[] args) {

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //话题模式消息
            Topic t=session.createTopic("speaking");
            MessageProducer producer = session.createProducer(t);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("快多点来人帮我扛两百斤麦子！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
