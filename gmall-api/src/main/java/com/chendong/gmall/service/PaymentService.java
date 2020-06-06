package com.chendong.gmall.service;

import com.chendong.gmall.bean.PaymentInfo;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendPaymentResult(String orderId, String result);
}
