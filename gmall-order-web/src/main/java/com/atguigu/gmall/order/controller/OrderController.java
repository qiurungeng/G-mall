package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @RequestMapping("submitOrder")
    @LoginRequired
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode,
                                    HttpServletRequest request, ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        ModelAndView mv=new ModelAndView("tradeFail");

        //检查交易码
        String success=orderService.checkTradeCode(memberId,tradeCode);
        //符合结算前提，制作结算订单
        if (success.equals("success")){

            //取出购物车结算项
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            //验价格(暂时未写验库存)
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")){
                    boolean ok=skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if (!ok)return mv;
                }
            }

            //根据用户id获得要购买的商品列表(购物车),和总价格，为确保结算数据为最新数据不能采用当前页面的数据！
            OmsOrder omsOrder=new OmsOrder();
            List<OmsOrderItem> omsOrderItems=generateOrderItemsFromCartItems(omsCartItems);

            //设置订单详情
            omsOrder.setOmsOrderItems(omsOrderItems);
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("快点发货");
            String outTradeNo="gmall"+System.currentTimeMillis()          //制作外部订单号
                    +new SimpleDateFormat("YYYYMMDDHHmmss").format(new Date());
            omsOrder.setOrderSn(outTradeNo);    //外部订单号
            for (OmsOrderItem omsOrderItem : omsOrder.getOmsOrderItems()) {
                omsOrderItem.setOrderSn(omsOrder.getOrderSn());
            }
            omsOrder.setPayAmount(totalAmount); //支付总额
            omsOrder.setOrderType(1);   //订单类型：0正常、1秒杀
            UmsMemberReceiveAddress address=userService.getReceiveAddressById(receiveAddressId);    //收货地址
            omsOrder.setReceiverProvince(address.getProvince());
            omsOrder.setReceiverCity(address.getCity());
            omsOrder.setReceiverRegion(address.getRegion());
            omsOrder.setReceiverDetailAddress(address.getDetailAddress());
            omsOrder.setReceiverName(address.getName());
            omsOrder.setReceiverPhone(address.getPhoneNumber());
            omsOrder.setReceiverPostCode(address.getPostCode());
            //一天后配送
            Calendar calendar=Calendar.getInstance();
            calendar.add(Calendar.DATE,1);
            Date deliveryTime=calendar.getTime();
            omsOrder.setDeliveryTime(deliveryTime);
            omsOrder.setSourceType(0);  //订单来源：0:PC、1:APP
            omsOrder.setStatus(0);  //订单状态：待付款
            omsOrder.setTotalAmount(totalAmount);

            //将订单和订单详情写入数据库
            orderService.saveOrder(omsOrder);

            //从定向到支付系统
            mv=new ModelAndView("redirect:http://payment.gmall.com:8087/");
            mv.addObject("outTradeNo",outTradeNo);
            mv.addObject("totalAmount",totalAmount);
            return mv;
        }else{
            return mv;
        }
    }

    @RequestMapping("toTrade")
    @LoginRequired(loginSuccessNeeded = true)
    public String toTrade(HttpServletRequest request, ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //将购物车集合转化为结算清单集合
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        List<OmsOrderItem> omsOrderItems=generateOrderItemsFromCartItems(omsCartItems);
        //收件人地址列表
        List<UmsMemberReceiveAddress> userAddressList = userService.getReceiveAddressByMemberId(memberId);

        modelMap.put("nickname",nickname);
        modelMap.put("omsOrderItems",omsOrderItems);
        modelMap.put("totalAmount",calculatePrice(omsCartItems));
        modelMap.put("userAddressList",userAddressList);
        //生成交易吗，为了在提交订单时候做校验
        String tradeCode=orderService.genTradeCode(memberId);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }

    private BigDecimal calculatePrice(List<OmsCartItem> omsCartItems){
        BigDecimal totalAmount=new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            //计算单品总价
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(BigDecimal.valueOf(omsCartItem.getQuantity())));
            //若被选中，累加入购物车选中商品总价
            if (omsCartItem.getIsChecked().equals("1")){
                totalAmount=totalAmount.add(omsCartItem.getTotalPrice());
            }
        }
        return totalAmount;
    }

    private List<OmsOrderItem> generateOrderItemsFromCartItems(List<OmsCartItem> omsCartItems){
        List<OmsOrderItem> omsOrderItems=new ArrayList<>();
        for (OmsCartItem cartItem : omsCartItems) {
            if (cartItem.getIsChecked().equals("1")){
                OmsOrderItem orderItem=new OmsOrderItem();
                orderItem.setProductId(cartItem.getProductId());               //商品Id
                orderItem.setProductName(cartItem.getProductName());    //商品名称
                orderItem.setProductPrice(cartItem.getPrice());         //单品价格
                orderItem.setProductCategoryId(cartItem.getProductCategoryId());
                orderItem.setProductPic(cartItem.getProductPic());
                orderItem.setProductQuantity(cartItem.getQuantity());
                orderItem.setRealAmount(cartItem.getTotalPrice());
                orderItem.setProductSkuCode("123456");
                orderItem.setProductSkuId(cartItem.getProductSkuId());
                orderItem.setProductSn("仓库对应的商品编号");                //仓库中的skuId

                omsOrderItems.add(orderItem);
            }
        }
        return omsOrderItems;
    }
}
