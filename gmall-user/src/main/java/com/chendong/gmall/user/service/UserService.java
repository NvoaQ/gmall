package com.chendong.gmall.user.service;

import com.chendong.gmall.user.bean.UmsMember;
import com.chendong.gmall.user.bean.UmsMemberReceiveAddress;

import java.util.List;

/**
 * @anthor chendong
 * @date 2019/10/24 - 20:17
 */
public interface UserService {
    //获取所有用户信息
    List<UmsMember> getAllUser();

    //通过memberId查询收货人地址信息
    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    //通过主键查询用户信息
    List<UmsMember> selectUmsMemberById(String id);

    //通过主键删除用户
    void deleteUmsMemberById(String id);

    //通过用户名和密码添加用户
    void insertUmsMemberByUsernameAndPassword(String username, String password);

    //通过主键id修改用户名和密码
    void updateUmsMemberById(String id,String username,String password);
}
