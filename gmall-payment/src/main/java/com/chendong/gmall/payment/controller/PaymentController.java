package com.chendong.gmall.payment.controller;

import com.chendong.gmall.annotations.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PaymentController {

    @RequestMapping("submit")
    @LoginRequire(loginSuccess = true)
    public String submit(String outTradeSn,String totalAmount,String orderId,HttpServletRequest request, ModelMap modelMap){


        return null;
    }

    @RequestMapping("index")
    @LoginRequire(loginSuccess = true)
    public String index(String outTradeSn,String totalAmount,String orderId,HttpServletRequest request, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("memberNickname");


        modelMap.put("outTradeSn",outTradeSn);
        modelMap.put("nickName",nickName);
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("orderId",orderId);

        return "index";
    }
}
