package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.OmsOrder;

public interface OrderService {
    String checkTradeCode(String memberId,String tradeCode);
    String genTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void orderPayUp(OmsOrder omsOrder);
}
