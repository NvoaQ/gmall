package com.chendong.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chendong.gmall.annotations.LoginRequire;
import com.chendong.gmall.bean.OmsCartItem;
import com.chendong.gmall.bean.PmsSkuInfo;
import com.chendong.gmall.service.CartService;
import com.chendong.gmall.service.SkuService;
import com.chendong.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @RequestMapping("checkCart")
    @LoginRequire(loginSuccess = false)
    public String checkCart(String isChecked,String skuId,ModelMap modelMap,HttpServletRequest request){
        String memberId = (String) request.getAttribute("memberId");
        //String nickname = (String) request.getAttribute("memberNickname");
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        cartService.checkCart(omsCartItem);

        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);

        //购物车页面总价格
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);

        return "innerCartListHtml";
    }

    @RequestMapping("cartList")
    @LoginRequire(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        String memberId = (String) request.getAttribute("memberId");
        //String nickname = (String) request.getAttribute("memberNickname");
        //用户没有登陆
        if(StringUtils.isBlank(memberId)){
            //查cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)) {
                List<OmsCartItem> omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                for (OmsCartItem omsCartItem : omsCartItems) {
                    omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                    omsCartItemList.add(omsCartItem);
                }
            }
        }else{
            //用户已登录
           omsCartItemList =  cartService.cartList(memberId);
        }

        modelMap.put("cartList",omsCartItemList);

        //购物车页面总价格
        BigDecimal totalAmount = getTotalAmount(omsCartItemList);
        modelMap.put("totalAmount",totalAmount);

        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItemList) {

        BigDecimal totalAmount = new BigDecimal("0");
        for(OmsCartItem omsCartItem:omsCartItemList){
            if(omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
            }
        }

        return totalAmount;
    }

    @RequestMapping("addToCart")
    @LoginRequire(loginSuccess = false)
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response){
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId, "");
        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        //判断用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        //String nickname = (String) request.getAttribute("memberNickname");
        if(StringUtils.isBlank(memberId)){
            //用户没有登陆
            //cookie里原有的购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isBlank(cartListCookie)) {
                //cookie为空
                omsCartItems.add(omsCartItem);
            }else {
                //cookie不为空
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断添加的购物车数据在cookie是否存在
                boolean exist = isCartExist(omsCartItems,omsCartItem);
                if(exist){
                    //之前添加过，更新购物车数量
                    for (OmsCartItem cartItem : omsCartItems) {
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                            //cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }
                    }
                }else{
                    //之前没有添加过，新增当前购物车
                    omsCartItems.add(omsCartItem);
                }
            }
            //更新cookie
            CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItems),60*60*72,true);

        }else{
            //用户已登录
            //从db中查购物车数据
            OmsCartItem omsCartItemFromDb = cartService.isCartExistByUser(memberId,skuId);
            if(omsCartItemFromDb == null){
                //没有添加过购物车
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("test");
                cartService.addCart(omsCartItem);
            }else{
                //添加过购物车
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }
            //同步缓存
            cartService.synCartCache(memberId);

        }
        return "redirect:success.html";
    }

    private boolean isCartExist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = false;
        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId = cartItem.getProductSkuId();
            if(productSkuId.equals(omsCartItem.getProductSkuId())){
                b = true;
            }
        }
        return b;
    }

}
