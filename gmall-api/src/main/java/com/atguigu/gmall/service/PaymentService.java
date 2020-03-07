package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void payUp(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheckQueue(String outTradeNo,int count);

    Map<String, Object> checkAlipayPayment(String out_trade_no);
}
