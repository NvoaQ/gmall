package com.chendong.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.chendong.gmall.annotations.LoginRequire;
import com.chendong.gmall.bean.OmsOrder;
import com.chendong.gmall.bean.PaymentInfo;
import com.chendong.gmall.payment.config.AlipayConfig;
import com.chendong.gmall.service.OrderService;
import com.chendong.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Reference
    PaymentService paymentService;

    @Reference
    OrderService orderService;

    @RequestMapping("sendResult")
    @ResponseBody
    public String sendPaymentResult(@RequestParam("orderId") String orderId) {
        paymentService.sendPaymentResult(orderId, "success");
        return "has been sent";
    }


    @RequestMapping("alipay/callback/return")
    @LoginRequire(loginSuccess = true)
    public String alipayCallbackReturn(HttpServletRequest request, ModelMap modelMap) {

        //回调请求中获取支付宝的参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();

        //验签，由于支付宝的2.0接口关闭了同步回调，不能在本地验签
        if (StringUtils.isNotBlank(sign)) {
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setAlipayTradeNo(trade_no);//支付宝流水号
            paymentInfo.setCallbackContent(call_back_content);//回调地址
            paymentInfo.setOrderSn(out_trade_no);//订单号
            paymentInfo.setSubject(subject);
            paymentInfo.setPaymentStatus("已付款");
            paymentInfo.setCallbackTime(new Date());
            //更改用户的支付信息
            paymentService.updatePaymentInfo(paymentInfo);
        }

        return "finish";
    }

    @RequestMapping("alipay/submit")
    @LoginRequire(loginSuccess = true)
    @ResponseBody
    public String alipaySubmit(String orderId, HttpServletRequest request, ModelMap modelMap) {
        //根据订单号查询总金额
        OmsOrder omsOrder = orderService.getOrderByOrderId(orderId);
        BigDecimal totalAmount = omsOrder.getTotalAmount();
        //去支付宝支付
        String form = "";
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderId);
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.01);
        map.put("subject", "gmall的商品");
        String mapJson = JSON.toJSONString(map);
        alipayRequest.setBizContent(mapJson);
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //生成并且保存用户的支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(omsOrder.getOrderSn());
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setSubject("gmall商品一件");
        paymentService.savePaymentInfo(paymentInfo);
        //提交表单到支付宝
        return form;
    }

    @RequestMapping("index")
    @LoginRequire(loginSuccess = true)
    public String index(String totalAmount, String orderId, HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("memberNickname");

        modelMap.put("nickName", nickName);
        modelMap.put("totalAmount", totalAmount);
        modelMap.put("orderId", orderId);
        return "index";
    }

}
