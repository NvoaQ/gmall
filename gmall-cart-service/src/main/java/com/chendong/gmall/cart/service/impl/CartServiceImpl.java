package com.chendong.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chendong.gmall.bean.OmsCartItem;
import com.chendong.gmall.cart.mapper.OmsCartItemMapper;
import com.chendong.gmall.service.CartService;
import com.chendong.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem isCartExistByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem item = omsCartItemMapper.selectOne(omsCartItem);
        return item;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if(StringUtils.isNotBlank(omsCartItem.getMemberId())) {
            omsCartItemMapper.insert(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example e =new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id",omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb,e);
    }

    /**
     * 刷新redis缓存中的购物车信息
     * @param memberId
     */
    @Override
    public void synCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
        //同步到redis缓存
        Jedis jedis = redisUtil.getJedis();
        HashMap<String, String> map = new HashMap<>();
        for(OmsCartItem item:omsCartItems){
            map.put(item.getProductSkuId(), JSON.toJSONString(item));
        }
        //先删除，再更新
        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId+":cart",map);
        jedis.close();

    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {

        List<OmsCartItem> select = new ArrayList<>();

        OmsCartItem item = new OmsCartItem();
        item.setMemberId(memberId);

        //从缓存redis中找购物车数据
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
            if(null!=hvals) {
                for (String hval : hvals) {
                    OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                    omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                    select.add(omsCartItem);
                }
            }else{
                select = omsCartItemMapper.select(item);
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            jedis.close();
        }
        return select;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("productSkuId",omsCartItem.getProductSkuId())
                            .andEqualTo("memberId",omsCartItem.getMemberId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);

        synCartCache(omsCartItem.getMemberId());


    }


}
