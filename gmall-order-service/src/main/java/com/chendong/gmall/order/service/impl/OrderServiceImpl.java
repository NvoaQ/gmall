package com.chendong.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chendong.gmall.bean.OmsOrder;
import com.chendong.gmall.bean.OmsOrderItem;
import com.chendong.gmall.order.mapper.OrderItemMapper;
import com.chendong.gmall.order.mapper.OrderMapper;
import com.chendong.gmall.service.OrderService;
import com.chendong.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Override
    public String genderTradeCode(String memberId) {
        String randnCode = UUID.randomUUID().toString();
        String tradeCode = randnCode + memberId;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            jedis.setex("user:" + memberId + ":tradeCode", 60 * 15, tradeCode);
        } finally {
            jedis.close();
        }

        return tradeCode;
    }

    @Override
    public String checkTradeCode(String tradeCode, String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String value = jedis.get("user:" + memberId + ":tradeCode");
            //这里考虑高并发下的问题，可以用lua脚本
            if (StringUtils.isNotBlank(value) && tradeCode.equals(value)) {
                jedis.del("user:" + memberId + ":tradeCode");
                return "success";
            } else {
                return "fail";
            }
        } finally {
            jedis.close();
        }
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        //保存订单
        orderMapper.insertSelective(omsOrder);
        //保存订单详情
        String omsOrderId = omsOrder.getId();
        List<OmsOrderItem> omsOrderItemList = omsOrder.getOmsOrderItemList();
        for (OmsOrderItem omsOrderItem : omsOrderItemList) {
            omsOrderItem.setOrderId(omsOrderId);
            orderItemMapper.insertSelective(omsOrderItem);
            //删除购物车数据
        }

    }

    @Override
    public OmsOrder getOrderByOrderId(String orderId) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(orderId);
        OmsOrder omsOrder1 = orderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    //定义一个消息监听器
    //监听消息队列: PAYMENT_RESULT_QUEUE
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        //更新订单信息
        if(StringUtils.isNotBlank(result)&&"success".equals(result)) {
            Example example = new Example(OmsOrder.class);
            example.createCriteria().andEqualTo("orderSn", orderId);
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOrderSn(orderId);
            //已支付
            omsOrder.setStatus("1");
            orderMapper.updateByExampleSelective(omsOrder,example);
        }

        }
    }
