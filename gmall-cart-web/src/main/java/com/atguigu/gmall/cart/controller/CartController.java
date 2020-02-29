package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;


    @RequestMapping("toTrade")
    @LoginRequired
    public String toTrade(HttpServletRequest request,ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        return "toTradeTest";
    }

    @RequestMapping("checkCart")
    @LoginRequired(loginSuccessNeeded = false)
    public String checkCart(String isChecked,String skuId,HttpServletRequest request,ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        cartService.checkCart(omsCartItem);
        //将最新数据从缓存中取出，渲染给内嵌页面
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        //设置购物车单项总价，并计算选中商品总价
        BigDecimal totalAmount=calculatePrice(omsCartItems);
        //购物车商品列表
        modelMap.put("cartList",omsCartItems);
        //购物车总价
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSuccessNeeded = false)
    public String cartList(HttpServletRequest request, ModelMap modelMap){
        List<OmsCartItem> omsCartItems = null;
        String userId="1";
        if (StringUtils.isNotBlank(userId)){
            //已经登录,查询db
            omsCartItems=cartService.cartList(userId);
        }else {
            //没有登录查询Cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                omsCartItems=JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }
        //设置购物车单项总价，并计算选中商品总价
        assert omsCartItems != null;
        BigDecimal totalAmount=calculatePrice(omsCartItems);
        //购物车商品列表
        modelMap.put("cartList",omsCartItems);
        //购物车总价
        modelMap.put("totalAmount",totalAmount);
        return "cartList";
    }

    @RequestMapping("addToCart")
    @LoginRequired(loginSuccessNeeded = false)
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response){
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

        List<OmsCartItem> omsCartItems;

        //将商品封装成购物车信息
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setModifyDate(omsCartItem.getCreateDate());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(quantity);
        omsCartItem.setIsChecked("1");

        //判断用户是否登录
        String memberId="1";
        //request.getAttribute("memberId");

        if (StringUtils.isBlank(memberId)){
            //用户未登录
            //取出cookie里原有的购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if (StringUtils.isBlank(cartListCookie)){
                //购物车cookie为空
                omsCartItems=new ArrayList<>();
                omsCartItems.add(omsCartItem);
            }else {
                //购物车cookie不为空
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
                //判断添加到购物车的商品数据在cookie中是否存在
                OmsCartItem exist=if_cart_exist(omsCartItems,omsCartItem);
                if (exist!=null){
                    //之前添加过，更新购物车中该商品的数量
                    exist.setQuantity(exist.getQuantity()+omsCartItem.getQuantity());
                }else {
                    //之前没有添加过，新增该商品到当前购物车
                    omsCartItems.add(omsCartItem);
                }
            }
            //更新cookie
            CookieUtil.setCookie(request,response,"cartListCookie",
                    JSON.toJSONString(omsCartItems),3600*72,true);
        }else {
            //用户已登录
            OmsCartItem cartItemFromDb=cartService.ifItemExistsInUserCart(memberId,skuId);
            if (cartItemFromDb==null){
                //该用户未添加过此商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setQuantity(quantity);
                omsCartItem.setMemberNickname("测试啊");
                cartService.addCartItem(omsCartItem);
            }else {
                //该用户已添加此商品
                cartItemFromDb.setQuantity(cartItemFromDb.getQuantity()+omsCartItem.getQuantity());
                cartService.updateCart(cartItemFromDb);
            }
            //同步缓存
            cartService.flushCartCache(memberId);
        }

        return "redirect:/success.html";
    }

    /**
     * 判断购物车中是否已存在相同商品，若存在则返回该购物车中商品项，否则返回null
     * @param omsCartItems 购物车中已有商品
     * @param omsCartItem 待判断是否存在于购物车的商品
     * @return 购物车中已存在的相同商品
     */
    private OmsCartItem if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        for (OmsCartItem cartItem : omsCartItems) {
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                return cartItem;
            }
        }
        return null;
    }

    /**
     * 设置购物车中单项商品的总价，并返回购物车内选中商品的总价
     * @param omsCartItems 购物车商品列表
     * @return 购物车商品总价
     */
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
}
