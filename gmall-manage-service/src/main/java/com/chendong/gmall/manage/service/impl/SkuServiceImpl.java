package com.chendong.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chendong.gmall.bean.PmsSkuAttrValue;
import com.chendong.gmall.bean.PmsSkuImage;
import com.chendong.gmall.bean.PmsSkuInfo;
import com.chendong.gmall.bean.PmsSkuSaleAttrValue;
import com.chendong.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.chendong.gmall.manage.mapper.PmsSkuImageMapper;
import com.chendong.gmall.manage.mapper.PmsSkuInfoMapper;
import com.chendong.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.chendong.gmall.service.SkuService;
import com.chendong.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        //插入skuInfo
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        //插入平台属性关联表
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        //插入销售属性关联表
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        //插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }


    }

    public PmsSkuInfo getSkuByIdFromDb(String skuId) {

        //sku商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //sku商品对象的图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);

        return skuInfo;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId,String ip) {

        //System.out.println(ip+"-->"+Thread.currentThread().getName()+"-->访问了商品的详情页请求");

        PmsSkuInfo pmsSkuInfo;

        //声明Jedis缓存对象
        Jedis jedis = null;

        try{
            //连接缓存
             jedis = redisUtil.getJedis();
            //查询缓存
            String skuKey="sku:"+skuId+":info";
            String skuJson = jedis.get(skuKey);

            //缓冲存在
            if(StringUtils.isNoneBlank(skuJson)){//if(skuJson!=null&&!skuJson.equals(""))
                //System.out.println(ip+"-->"+Thread.currentThread().getName()+"从缓存中获取了商品的详情页请求");
                //将从redis查询的json字符串转为Java对象PmsSkuInfo
                pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
            }else{
                //如果缓存中没有，查询mysql，但是可能出现缓存击穿问题
                //System.out.println(ip+"-->"+Thread.currentThread().getName()+"发现缓存中没有，申请缓存的分布式锁");

                //设置redis的分布式锁
                String toKen = UUID.randomUUID().toString();
                //拿到锁的线程有1秒的过期时间
                String OK = jedis.set("sku:"+skuId+":lock",toKen,"nx","px",1000*1);

                //如果设置成功，有权在10秒的过期时间内访问数据库
                if(StringUtils.isNotBlank(OK)&&OK.equals("OK")){
                    //System.out.println(ip+"-->"+Thread.currentThread().getName()+"有权在10秒的过期时间内访问数据库"+"sku:"+skuId+":lock");
                    pmsSkuInfo = getSkuByIdFromDb(skuId);

                    //延迟一段时间后将数据存入redis
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    if(pmsSkuInfo!=null){
                        //mysql查询结果存入redis
                        jedis.set("sku:"+skuId+":info",JSON.toJSONString(pmsSkuInfo));
                    }else{
                        //数据库中不存在该sku
                        //为了防止缓存穿透，将null或空字符串设置给redis
                        jedis.setex("sku:"+skuId+":info",60*3,JSON.toJSONString(""));
                    }

                    //在访问mysql后，将mysql的分布锁释放
                   // System.out.println(ip+"-->"+Thread.currentThread().getName()+"使用完毕，将锁归还"+"sku:"+skuId+":lock");

                    String lockToken = jedis.get("sku:"+skuId+":lock");
                    //判断lockToken是否为本线程的锁
                    if(StringUtils.isNotBlank(lockToken)&&lockToken.equals(toKen)) {
                        //释放锁
                        jedis.del("sku:" + skuId + ":lock");
                    }

                }else{
                    //设置失败，自旋（该线程在睡眠几秒后，重新访问）
                    //System.out.println(ip+"-->"+Thread.currentThread().getName()+"没有拿到锁，开始自旋"+"sku:");
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    //线程结束，重新访问（不会另外新建线程）
                   return getSkuById(skuId,ip);
                }
            }
        }finally {
            jedis.close();
        }

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);

        }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        pmsSkuInfo.setPrice(price);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        if(pmsSkuInfo1!=null) {
            if (pmsSkuInfo1.getPrice().compareTo(price) == 0) {
                return true;
            }
        }

        return false;
    }
}
