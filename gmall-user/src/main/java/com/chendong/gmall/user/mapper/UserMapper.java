package com.chendong.gmall.user.mapper;

import com.chendong.gmall.user.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @anthor chendong
 * @date 2019/10/24 - 20:20
 */
public interface UserMapper extends Mapper<UmsMember> {
    //获取用户的所有信息
    List<UmsMember> selectAllUser();
    //通过主键查询用户信息
    List<UmsMember> selectUmsMemberById(String id);
    //通过主键删除用户
    void deleteUmsMemberById(String id);
    //通过用户名和密码添加用户
    void insertUmsMemberByUsernameAndPassword(String username, String password);
    //通过主键修改用户的姓名和密码
    void updateUmsMemberById(String id, String username, String password);

}
