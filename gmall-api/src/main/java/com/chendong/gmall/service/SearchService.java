package com.chendong.gmall.service;

import com.chendong.gmall.bean.PmsSearchParam;
import com.chendong.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
