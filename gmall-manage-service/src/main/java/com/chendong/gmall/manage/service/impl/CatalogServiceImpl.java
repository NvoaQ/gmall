package com.chendong.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chendong.gmall.bean.PmsBaseCatalog1;
import com.chendong.gmall.bean.PmsBaseCatalog2;
import com.chendong.gmall.bean.PmsBaseCatalog3;
import com.chendong.gmall.manage.mapper.PmsCatalog2Mapper;
import com.chendong.gmall.manage.mapper.PmsCatalog3Mapper;
import com.chendong.gmall.manage.mapper.PmsCatalog1Mapper;
import com.chendong.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    PmsCatalog1Mapper pmsCatalog1Mapper;

    @Autowired
    PmsCatalog2Mapper pmsCatalog2Mapper;

    @Autowired
    PmsCatalog3Mapper pmsCatalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        //调用通用Mapper查询全部一级目录分类信息
        return pmsCatalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        //通过一级目录构建二级目录
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);
        //调用通用Mapper，查询全部二级目录分类信息
        return pmsCatalog2Mapper.select(pmsBaseCatalog2);
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        //通过二级目录构建三级目录
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        //调用通用Mapper，查询全部三级目录分类信息
        return pmsCatalog3Mapper.select(pmsBaseCatalog3);
    }
}
