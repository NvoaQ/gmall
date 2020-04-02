package com.chendong.gmall.service;

import com.chendong.gmall.bean.OmsOrder;

public interface OrderService {

    String genderTradeCode(String memberId);

    String checkTradeCode(String tradeCode,String memberId);

    void saveOrder(OmsOrder omsOrder);
}
