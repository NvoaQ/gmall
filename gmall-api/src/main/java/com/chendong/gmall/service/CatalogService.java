package com.chendong.gmall.service;

import com.chendong.gmall.bean.PmsBaseCatalog1;
import com.chendong.gmall.bean.PmsBaseCatalog2;
import com.chendong.gmall.bean.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {

    //查询一级目录分类信息
    List<PmsBaseCatalog1> getCatalog1();

    //根据一级目录，查询二级目录分类信息
    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    //根据二级目录，查询三级目录分类信息
    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);

}
