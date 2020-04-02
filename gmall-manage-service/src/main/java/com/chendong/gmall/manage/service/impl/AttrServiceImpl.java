package com.chendong.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chendong.gmall.bean.PmsBaseAttrInfo;
import com.chendong.gmall.bean.PmsBaseAttrValue;
import com.chendong.gmall.bean.PmsBaseSaleAttr;
import com.chendong.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.chendong.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.chendong.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.chendong.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService{

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    /**
     * 根据三级分类id查询平台属性和平台属性值
     * @param catalog3Id 三级分类id
     * @return 平台属性和平台属性值
     */
    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {

        //1.平台属性
        //根据三级目录查询
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        //调用通用mapper，传入待查询的平台属性对象，返回平台属性表
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        //2.平台属性值
        //平台属性表对应着平台属性值，对平台属性表进行遍历，给每个平台属性赋予平台属性值
        for(PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos){
            //平台属性值
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            //调用通用mapper查询，并返回平台属性值表
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            //将对应的平台属性值的表存入平台属性
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }

        //返回平台属性和平台属性值
        return pmsBaseAttrInfos;
    }

    /**
     * 根据平台属性查询平台属性值
     * @param attrId 平台属性
     * @return 平台属性值
     */
    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        //平台属性值
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        //调用通用mapper查询
        return pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
    }

    /**
     *
     * @param pmsBaseAttrInfo
     * @return
     */
    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        //根据平台销售属性获取平台属性值
        String id = pmsBaseAttrInfo.getId();

        if(StringUtils.isBlank(id)){
            //平台属性值id为空，则为保存
            //保存空的平台属性
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);//insertSelective是否将null插入数据库

            //保存平台属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        } else {
            //平台属性id不为空，则为修改

            //属性修改
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

            //属性值修改
            //按照属性id删除所有属性值
            PmsBaseAttrValue pmsBaseAttrValueDel = new PmsBaseAttrValue();
            pmsBaseAttrValueDel.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValueDel);

            //删除后，将新的属性值插入
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);

            }

        }

        return "success";
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    /**
     *
     * @param valueIdSet 搜索结果中去重后的属性值id
     * @return
     */
    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet) {

        //将valueIdSet转换成字符串
        String valueId = StringUtils.join(valueIdSet, ",");

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueId);

        return pmsBaseAttrInfos;
    }
}
