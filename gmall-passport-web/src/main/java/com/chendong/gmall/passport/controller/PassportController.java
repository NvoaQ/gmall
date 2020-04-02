package com.chendong.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chendong.gmall.bean.UmsMember;
import com.chendong.gmall.service.UserService;
import com.chendong.gmall.util.JwtUtil;
import com.chendong.gmall.utils.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request) {
        //微博开发平台的接口定义
        String client_id = "850529739";
        String client_secret = "ef666ca3d90d08f753da32e3ee898f7f";
        String redirect_uri = "http://passport.gmall.com:8085/vlogin";
        String url = "https://api.weibo.com/oauth2/access_token?";
        Map<String,String> map = new HashMap<>();
        map.put("client_id",client_id);
        map.put("client_secret",client_secret);
        map.put("redirect_uri",redirect_uri);
        map.put("code",code);
        map.put("grant_type","authorization_code");
        //获取accessToken
        String accessTokenJson = HttpclientUtil.doPost(url,map);
        Map<String,Object> accessMap = JSON.parseObject(accessTokenJson, Map.class);
        String access_token = (String) accessMap.get("access_token");
        String uid = (String) accessMap.get("uid");
        //根据accessToken和uid获取用户信息
        String getUserInfoUrl = "https://api.weibo.com/2/users/show.json?";
        String getTotalUrl = getUserInfoUrl+"access_token="+access_token+"&uid="+uid;
        String userInfoJson = HttpclientUtil.doGet(getTotalUrl);
        Map<String,Object> userInfoMap = JSON.parseObject(userInfoJson, Map.class);
        //筛选感兴趣的内容
        UmsMember umsMember = new UmsMember();
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceType("2");
        umsMember.setNickname((String) userInfoMap.get("screen_name"));
        umsMember.setSourceId((String) userInfoMap.get("idstr"));
        umsMember.setCity((String) userInfoMap.get("location"));
        //防止重复添加
        UmsMember umsMemberCheckParam = new UmsMember();
        umsMemberCheckParam.setSourceId(umsMember.getSourceId());
        UmsMember umsMemberCheck = userService.checkOauthUser(umsMemberCheckParam);
        //数据库没有
        if(umsMemberCheck==null){
            //添加到数据库中
            userService.addOauthUser(umsMember);
        }else{
            umsMember = umsMemberCheck;
        }
        //获取token
        String token = getToken(umsMember, request);
        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }

    private String  getToken(UmsMember umsMember,HttpServletRequest request) {
        String token = "";
        //调用用户服务验证登录
        UmsMember umsMember1 = userService.loginCheck(umsMember);
        if (umsMember1 != null) {
            //已登录
            Map<String, Object> map = new HashMap<>();
            map.put("memberId", umsMember1.getId());
            map.put("memberNickname", umsMember1.getNickname());
            //获取ip
            String remoteAddr = request.getRemoteAddr();
            if (StringUtils.isBlank(remoteAddr)) {
                remoteAddr = "127.0.0.1";
            }
            //生成token
            token = JwtUtil.encode("gmall-chendong-hdu.com", map, remoteAddr);
            //存入缓存一份
            userService.addUserToken(token, umsMember1.getId());
        }
        return token;
    }


    /**
     * 验证token的真伪
     *
     * @param token
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String remoteAddr) {
        //调用jwt验证token
        //服务器密钥：gmall-chendong-hdu.com
        //salt：remoteAddr
        Map<String, Object> decode = JwtUtil.decode(token, "gmall-chendong-hdu.com", remoteAddr);
        Map<String,String> map = new HashMap<>();
        //解析token
        if(decode!=null){
            map.put("status","success");
            map.put("memberId",(String)decode.get("memberId"));
            map.put("memberNickname",(String) decode.get("memberNickname"));
        }else{
            map.put("status","fail");
        }
        return JSON.toJSONString(map);
    }

    /**
     * 发行token
     *
     * @param umsMember
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token = getToken(umsMember, request);
        if(StringUtils.isBlank(token)){
            //没有登录
            return "fail";
        }
        return token;
    }

    @RequestMapping("index")
    public String index(String originUrl, ModelMap modelMap) {
        modelMap.put("originUrl", originUrl);
        return "index";
    }
}
