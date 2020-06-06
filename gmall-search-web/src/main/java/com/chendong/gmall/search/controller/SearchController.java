package com.chendong.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chendong.gmall.annotations.LoginRequire;
import com.chendong.gmall.bean.*;
import com.chendong.gmall.service.AttrService;
import com.chendong.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){//三级分类id、关键字、平台属性的集合

        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);

        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

        //用set集合将不重复的pmsSearchSkuInfo的PmsSkuAttrValue的valueId取出来
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> pmsSkuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValueList) {
                valueIdSet.add(pmsSkuAttrValue.getValueId());
            }
        }

        //将去重后的valueIdSet去中查询平台属性表
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList",pmsBaseAttrInfos);

        //点击商品筛选后，筛选条件为平台属性值表中应该去除的部分
        //合并添加面包屑功能
        String[] delvalueId = pmsSearchParam.getValueId();
        if (delvalueId != null) {
            //创建面包屑的集合
            List<PmsSearchCrumb> crumbList = new ArrayList<>();
            for (String s : delvalueId) {
                //对应每一个被筛选的条件，用迭代器去删除在平台属性表中的值
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                //同时对应每一个筛选条件就应该有一个面包屑
                PmsSearchCrumb crumb = new PmsSearchCrumb();
                crumb.setValueId(s);
                crumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam,delvalueId));

                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String id = pmsBaseAttrValue.getId();
                        if (s.equals(id)) {
                            //给面包屑赋值
                            crumb.setValueName(pmsBaseAttrValue.getValueName());
                            //用迭代器移除当前的属性即pmsBaseAttrInfo
                            iterator.remove();
                        }
                    }
                }
                crumbList.add(crumb);
            }
            modelMap.put("attrValueSelectedList",crumbList);
        }

        //面包屑
        //PmsSearchParam
        //delvalueId
//        if(delvalueId!=null){
//            //创建面包屑
//            List<PmsSearchCrumb> crumbList = new ArrayList<>();
//            for (String id : delvalueId) {
//                PmsSearchCrumb crumb = new PmsSearchCrumb();
//                crumb.setValueId(id);
//                crumb.setValueName(id);
//                crumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam,delvalueId));
//                crumbList.add(crumb);
//            }
//            modelMap.put("attrValueSelectedList",crumbList);
//        }

        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);

        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword",keyword);
        }

        return "list";
    }

    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam,String[] delvalueId) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if(skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                for(String value:delvalueId){
                    if(!pmsSkuAttrValue.equals(value)) {
                        urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
                    }
                }

            }
        }

        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if(skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
            }
        }

        return urlParam;
    }

    @RequestMapping("index")
    @LoginRequire(loginSuccess = false)
    public String index(){
        return "index";
    }

}
