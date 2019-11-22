package com.chendong.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chendong.gmall.bean.PmsProductImage;
import com.chendong.gmall.bean.PmsProductInfo;
import com.chendong.gmall.bean.PmsProductSaleAttr;
import com.chendong.gmall.manage.util.PmsUploadUtil;
import com.chendong.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){

        return spuService.spuImageList(spuId);

    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        return spuService.spuSaleAttrList(spuId);

    }

    //文件上传
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        //将图片或者视频上传到分布式的文件存储系统
        //将图片的存储路径返回给页面
        String imgUrl= PmsUploadUtil.uploadImage(multipartFile);
        System.out.println(imgUrl);
        return imgUrl;

    }

    //保存spu信息
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){

        spuService.saveSpuInfo(pmsProductInfo);

        return "success";

    }

    //返回商品的spu信息
    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){

        return spuService.spuList(catalog3Id);

    }




}
