package com.chendong.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chendong.gmall.bean.PmsBaseCatalog1;
import com.chendong.gmall.bean.PmsBaseCatalog2;
import com.chendong.gmall.bean.PmsBaseCatalog3;
import com.chendong.gmall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin//设置跨域访问
public class CatalogController {

    @Reference
    CatalogService catalogService;

    //查询一级目录分类信息
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1() {

        List<PmsBaseCatalog1> catalog1s = catalogService.getCatalog1();
        return catalog1s;

    }

    //根据一级目录，查询二级目录分类信息
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {

        List<PmsBaseCatalog2> catalog2s = catalogService.getCatalog2(catalog1Id);
        return catalog2s;

    }

    //根据二级目录，查询三级目录分类信息
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {

        List<PmsBaseCatalog3> catalog3s = catalogService.getCatalog3(catalog2Id);
        return catalog3s;

    }
}
