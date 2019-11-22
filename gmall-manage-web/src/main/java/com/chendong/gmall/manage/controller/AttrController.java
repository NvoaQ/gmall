package com.chendong.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chendong.gmall.bean.PmsBaseAttrInfo;
import com.chendong.gmall.bean.PmsBaseAttrValue;
import com.chendong.gmall.bean.PmsBaseSaleAttr;
import com.chendong.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class AttrController {

    @Reference
    AttrService attrService;

    //添加商品的spu信息
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs= attrService.baseSaleAttrList();
        return pmsBaseSaleAttrs;
    }

    //根据属性id获取平台属性值表
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){
        List<PmsBaseAttrValue> pmsBaseAttrValues= attrService.getAttrValueList(attrId);
        return pmsBaseAttrValues;
    }

    //根据三级分类信息表，获取平台属性表
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.attrInfoList(catalog3Id);
        return pmsBaseAttrInfos;
    }

    //销售属性的保存
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

       String success = attrService.saveAttrInfo(pmsBaseAttrInfo);

        return success;
    }


}
