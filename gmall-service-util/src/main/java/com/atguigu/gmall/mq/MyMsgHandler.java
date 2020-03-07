package com.atguigu.gmall.mq;

import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MyMsgHandler {
//
//    public static void sendQueueMsg(String queueName, Message message){
//        Connection connection=null;
//        Session session=null;
//        try {
//            connection = new ActiveMQUtil().getConnectionFactory().createConnection();
//            session = connection.createSession(true, Session.SESSION_TRANSACTED);//可回滚
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }
//        try {
//            assert session != null;
//            Queue queue = session.createQueue(queueName);
//            MessageProducer producer = session.createProducer(queue);
//            producer.send(message);
//            session.commit();
//        } catch (JMSException e) {
//            try {
//                session.rollback();
//            } catch (JMSException ex) {
//                ex.printStackTrace();
//            }
//        } finally {
//            try {
//                if (connection!=null)connection.close();
//            } catch (JMSException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
