package com.chendong.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chendong.gmall.annotations.LoginRequire;
import com.chendong.gmall.bean.OmsCartItem;
import com.chendong.gmall.bean.OmsOrder;
import com.chendong.gmall.bean.OmsOrderItem;
import com.chendong.gmall.bean.UmsMemberReceiveAddress;
import com.chendong.gmall.service.CartService;
import com.chendong.gmall.service.OrderService;
import com.chendong.gmall.service.SkuService;
import com.chendong.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @RequestMapping("submitOrder")
    @LoginRequire(loginSuccess = true)
    public ModelAndView submitOrder(String deliveryAddressId,BigDecimal totalAmount,String tradeCode,HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("memberNickname");
        //检查交易码
        String success = orderService.checkTradeCode(tradeCode,memberId);

        //提交订单
        if(success.equals("success")){
            List<OmsOrderItem> omsOrderItemList = new ArrayList<>();
            //订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickName);
            omsOrder.setNote("速度发货");
            String outTradeSn = "chendong-gamll" + System.currentTimeMillis();
            omsOrder.setOrderSn(outTradeSn);
            omsOrder.setTotalAmount(totalAmount);
            omsOrder.setTotalAmount(totalAmount);

            //通过主键id查询收获地址
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(deliveryAddressId);
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());

            //配送日期
            Calendar today = Calendar.getInstance();
            today.add(Calendar.DATE,1);//明天
            Date tomorrow = today.getTime();
            omsOrder.setDeliveryTime(tomorrow);

            //封装购物车到订单详情
            List<OmsCartItem> omsCartItemList = cartService.cartList(memberId);

            for (OmsCartItem omsCartItem : omsCartItemList) {
                //每一个在购物车标记的商品都生成一个订单详情
                if(omsCartItem.getIsChecked().equals("1")){
                    //订单详情
                    OmsOrderItem omsOrderItem = new OmsOrderItem();

                    //检验价格是否正确
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if(b==false){
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    }
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setOrderSn(omsCartItem.getProductSn());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsCartItem.setQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());

                    omsOrderItemList.add(omsOrderItem);
                }
            }
            //封装订单详情到订单
            omsOrder.setOmsOrderItemList(omsOrderItemList);

            //将订单写入数据库
            orderService.saveOrder(omsOrder);

            modelMap.put("orderList",omsOrder);

            //重定向到支付系统
            ModelAndView mv = new ModelAndView("redirect:http://payment.gmall.com:8087/index?outTradeSn="+outTradeSn);
            mv.addObject("totalAmount",totalAmount);
            mv.addObject("orderId",outTradeSn);
            return mv;

        }else{
            //订单提交失败
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("memberNickname");

        //结算页面不会向数据库写入订单信息
        //用户的收货地址
        List<UmsMemberReceiveAddress> umsMemberReceiveAddressList = userService.getReceiveAddressByMemberId(memberId);

        //用户的购物车详情
        List<OmsCartItem> cartList = cartService.cartList(memberId);
        //只提交购物车中已选择的
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        for (OmsCartItem item : cartList) {
            if(item.getIsChecked().equals("1")){
                OmsCartItem orderItem = new OmsCartItem();
                orderItem.setProductPic(item.getProductPic());
                orderItem.setProductName(item.getProductName());
                omsCartItemList.add(item);
            }
        }

        BigDecimal totalAmount = getTotalAmount(omsCartItemList);
        //生成订单的交易码，防止重复提交
        String tradeCode = orderService.genderTradeCode(memberId);
        modelMap.put("orderDetailList",omsCartItemList);
        modelMap.put("UmsMemberReceiveAddressList",umsMemberReceiveAddressList);
        modelMap.put("nickName",nickName);
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItemList) {

        BigDecimal totalAmount = new BigDecimal("0");
        for(OmsCartItem omsCartItem:omsCartItemList){
            if(omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
            }
        }

        return totalAmount;
    }

}
