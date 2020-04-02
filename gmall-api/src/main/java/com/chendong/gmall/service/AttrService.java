package com.chendong.gmall.service;

import com.chendong.gmall.bean.PmsBaseAttrInfo;
import com.chendong.gmall.bean.PmsBaseAttrValue;
import com.chendong.gmall.bean.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

public interface AttrService {

    //根据三级分类信息表，获取平台属性表
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    //根据属性id获取平台属性值表
    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    //销售属性的保存
    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    //添加商品的spu信息
    List<PmsBaseSaleAttr> baseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet);
}
