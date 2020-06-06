package com.chendong.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chendong.gmall.bean.PaymentInfo;
import com.chendong.gmall.payment.mapper.PaymentInfoMapper;
import com.chendong.gmall.service.PaymentService;
import com.chendong.gmall.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {
        String orderSn = paymentInfo.getOrderSn();
        Example e = new Example(PaymentInfo.class);
        e.createCriteria().andEqualTo("orderSn", orderSn);
        //更新支付信息
        paymentInfoMapper.updateByExampleSelective(paymentInfo, e);
        //更新支付信息=》更新订单=》更新仓库
        sendPaymentResult(orderSn, "success");
    }

    @Override
    public void sendPaymentResult(String orderId, String result) {
        //1.获取activeMQ的连接工厂
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection = null;
        try {
            //2.创建连接
            connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");

            //订单信息
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId", orderId);
            mapMessage.setString("result", result);
            MessageProducer producer = session.createProducer(paymentResultQueue);
            producer.send(mapMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
