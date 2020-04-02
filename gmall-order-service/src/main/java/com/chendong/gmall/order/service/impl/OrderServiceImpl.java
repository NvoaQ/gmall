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
import redis.clients.jedis.Jedis;

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
        String tradeCode = randnCode+memberId;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            jedis.setex("user:"+memberId+":tradeCode",60*15,tradeCode);
        }finally {
            jedis.close();
        }

        return tradeCode;
    }

    @Override
    public String checkTradeCode(String tradeCode,String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String value = jedis.get("user:" + memberId + ":tradeCode");
            //这里考虑高并发下的问题，可以用lua脚本
            if(StringUtils.isNotBlank(value)&&tradeCode.equals(value)) {
                jedis.del("user:" + memberId + ":tradeCode");
                return "success";
            }else {
                return "fail";
            }
        }finally {
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
}
