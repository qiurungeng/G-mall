package com.atguigu.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class TestMqProducer {
    public static void main(String[] args) {

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("drink");

            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("谁帮我卢本伟倒一杯卡布奇诺？");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();
            System.out.println("我卢本伟下线了");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
