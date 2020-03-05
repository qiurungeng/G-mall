package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService;

    @RequestMapping("/")
    @LoginRequired
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId=(String)request.getAttribute("memberId");
        String nickname=(String)request.getAttribute("nickname");


        modelMap.put("nickname",nickname);
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);

        return "index";
    }

    @RequestMapping("wx/submit")
    @LoginRequired
    public String wx(){
        return "";
    }

    /**
     * 前往支付宝付款
     * @param outTradeNo 订单号
     * @param totalAmount 订单总额
     * @return 支付页面
     */
    @RequestMapping("alipay/submit")
    @LoginRequired
    @ResponseBody
    public String alipay(String outTradeNo,BigDecimal totalAmount,HttpServletRequest request,ModelMap modelMap){
        //获得一个支付宝请求的客户端（不是链接，而是封装好了http请求的表单请求）
        String form="";
        AlipayTradePagePayRequest alipayRequest=new AlipayTradePagePayRequest();  //创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);    //同步回调地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);    //异步通知地址
        Map<String,Object> map=new HashMap<>(); //必选请求参数
        map.put("out_trade_no",outTradeNo);     //订单号
        map.put("product_code","FAST_INSTANT_TRADE_PAY");   //支付宝签约产品码，固定
//            map.put("total_amount",totalAmount);      //订单总额
        map.put("total_amount",0.01);           //模拟订单总额
        map.put("subject","谷粒商城收款中心");     //订单描述
        String param= JSON.toJSONString(map);
        alipayRequest.setBizContent(param);
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //生成并保存用户的支付信息
        OmsOrder omsOrder=orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("一笔新的订单");
        paymentService.savePaymentInfo(paymentInfo);

        //提交请求到支付宝
        return form;
    }


    @RequestMapping("alipay/callback/return")
    @LoginRequired
    @ResponseBody
    public String alipayCallbackReturn(String outTradeNo,BigDecimal totalAmount,HttpServletRequest request,ModelMap modelMap){

        //回调请求中获取支付宝发来的参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();

        //通过支付宝的paramsMap进行验证，2.0版本的接口将paramMap参数去掉了，导致同步请求没法验签
        if (StringUtils.isNotBlank(sign)){
            //验签成功
            PaymentInfo paymentInfo=new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no); //支付宝交易凭证号
            paymentInfo.setCallbackContent(call_back_content);  //回调请求字符串
            paymentInfo.setCallbackTime(new Date());
            //更新用户的支付状态为已付款
            paymentService.payUp(paymentInfo);
        }

        //支付成功后的系统服务：订单服务、库存服务、物流

        return "finish";
    }
}
