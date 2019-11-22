package com.chendong.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chendong.gmall.bean.UmsMember;
import com.chendong.gmall.bean.UmsMemberReceiveAddress;
import com.chendong.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @anthor chendong
 * @date 2019/10/24 - 20:14
 */
@Controller
public class UserController {

    @Reference
    UserService userService;

    //通过主键查询用户信息
    @RequestMapping("selectUmsMemberById")
    @ResponseBody
    public List<UmsMember> selectUmsMemberById(String id){

        List<UmsMember> umsMemberList = userService.selectUmsMemberById(id);

        return umsMemberList;

    }

    //通过主键删除用户的信息
    @RequestMapping("deleteUmsMemberById")
    @ResponseBody
    public void deleteUmsMemberById(String id){

        userService.deleteUmsMemberById(id);

    }

    //通过用户名和密码添加用户信息
    @RequestMapping("insertUmsMemberByUsernameAndPassword")
    @ResponseBody
    public void insertUmsMemberByUsernameAndPassword(String username,String password){

        userService.insertUmsMemberByUsernameAndPassword(username,password);

    }

    //通过主键修改用户信息
    @RequestMapping("updateUmsMemberById")
    @ResponseBody
    public void updateUmsMemberById(String id,String username,String password){

        userService.updateUmsMemberById(id,username,password);

    }

    //通过memberId查询收货人地址信息
    @RequestMapping("getReceiveAddressByMemberId")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId){

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getReceiveAddressByMemberId(memberId);

        return umsMemberReceiveAddresses;
    }

    //获取所有用户信息
    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){

        List<UmsMember> umsMembers = userService.getAllUser();

        return umsMembers;
    }

    @RequestMapping("index")
    @ResponseBody
    public String index(@RequestParam("yourname") String name){
        return "hello"+"\n"+name;
    }


}
