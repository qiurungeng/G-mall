package com.atguigu.gmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void paymentCheckResultConsumer(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        int count= mapMessage.getInt("count"); //剩余检查次数
        //调用支付宝检查接口
        Map<String,Object> resultMap=paymentService.checkAlipayPayment(out_trade_no);

        if (resultMap!=null&&!resultMap.isEmpty()){
            String trade_status=(String) resultMap.get("trade_status");
            if (StringUtils.isNotBlank(trade_status)&&trade_status.equals("TRADE_SUCCESS")){
                PaymentInfo paymentInfo=new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);   //订单号
                paymentInfo.setPaymentStatus("已支付");    //支付状态
                paymentInfo.setAlipayTradeNo((String) resultMap.get("trade_no")); //支付宝交易凭证号
                paymentInfo.setCallbackContent((String) resultMap.get("call_back_content"));  //回调请求字符串
                paymentInfo.setCallbackTime(new Date());
                paymentService.payUp(paymentInfo);
                System.out.println("支付成功，调用支付服务，修改支付信息和发送支付成功的队列");
                return;
            }
        }
        //继续发送延迟检查任务，计算延迟时间等
        if (count>0){
            //继续发送延迟检查任务，计算延迟时间等
            System.out.println("没支付成功，继续发送延迟检查消息，还剩检查次数："+count);
            count--;
            paymentService.sendDelayPaymentResultCheckQueue(out_trade_no,count);
        }else {
            System.out.println("检查次数用尽，放弃检查");
        }
    }
}
