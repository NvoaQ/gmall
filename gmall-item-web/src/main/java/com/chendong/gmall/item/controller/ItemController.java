package com.chendong.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chendong.gmall.bean.PmsProductSaleAttr;
import com.chendong.gmall.bean.PmsSkuInfo;
import com.chendong.gmall.bean.PmsSkuSaleAttrValue;
import com.chendong.gmall.service.SkuService;
import com.chendong.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map, HttpServletRequest request) {
        //获取访问网页的ip地址
        String ip = request.getRemoteAddr();
        //根据skuId从sku库存单元表中取信息
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId, ip);
        //将取出的sku对象放入item.html页面中
        map.put("skuInfo", pmsSkuInfo);
        //sku对象对应的销售属性列表放入页面中
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), pmsSkuInfo.getId());
        map.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);
        //没使用静态化，如果使用静态化，应该将查到的hash表放到js文件中
        //查询当前sku所在的spu的其他sku的集合的hash表
        HashMap<String, String> skuSaleAttrHashMap = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
        //拼串 239|241| 106    <==>    销售属性值id|销售属性值id| skuid
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k = "";
            //skuId
            String v = skuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";//239|241|
            }
            skuSaleAttrHashMap.put(k, v);
        }
        //转化为JSON字符串
        String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHashMap);
        //将sku的销售属性hash表放到页面
        map.put("skuSaleAttrHashJsonStr", skuSaleAttrHashJsonStr);

        return "item";
    }

    @RequestMapping("index")
    public String index(ModelMap modelMap) {

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("循环数据" + i);
        }

        modelMap.put("list", list);
        modelMap.put("hello", "hello thymeleaf!");

        modelMap.put("check", "1");
        return "index";
    }

}
