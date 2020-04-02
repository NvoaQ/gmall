package com.chendong.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chendong.gmall.bean.UmsMember;
import com.chendong.gmall.bean.UmsMemberReceiveAddress;
import com.chendong.gmall.service.UserService;
import com.chendong.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.chendong.gmall.user.mapper.UserMapper;
import com.chendong.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @anthor chendong
 * @date 2019/10/24 - 20:18
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 通过主键查询用户信息
     *
     * @param id 主键
     * @return
     */
    @Override
    public List<UmsMember> selectUmsMemberById(String id) {
        UmsMember umsMember = new UmsMember();
        umsMember.setId(id);
        List<UmsMember> umsMembers = userMapper.select(umsMember);

        return umsMembers;
    }

    /**
     * 通过主键删除用户
     *
     * @param id 主键
     */
    @Override
    public void deleteUmsMemberById(String id) {
        UmsMember umsMember = new UmsMember();
        umsMember.setId(id);

        userMapper.delete(umsMember);
    }

    /**
     * 通过用户名和密码添加用户
     *
     * @param username 用户名
     * @param password 密码
     */
    @Override
    public void insertUmsMemberByUsernameAndPassword(String username, String password) {
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(username);
        umsMember.setPassword(password);
        userMapper.insert(umsMember);
    }

    /**
     * 通过主键修改用户的姓名和密码
     *
     * @param id       主键
     * @param username 用户名
     * @param password 密码
     */
    @Override
    public void updateUmsMemberById(String id, String username, String password) {
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(username);
        umsMember.setPassword(password);
        Example e = new Example(UmsMember.class);
        e.createCriteria().andEqualTo("id", id);
        userMapper.updateByExample(umsMember, e);
    }

    @Override
    public UmsMember loginCheck(UmsMember umsMember) {

        //缓存
        Jedis jedis = redisUtil.getJedis();
        String umsMeberInfoCache = jedis.get("user:" + umsMember.getPassword() + ":info");
        if (StringUtils.isNotBlank(umsMeberInfoCache)) {
            return JSON.parseObject(umsMeberInfoCache, UmsMember.class);
        }
        //缓存中没有，查数据库
        UmsMember umsMemberInfoDb = userMapper.selectOne(umsMember);
        //设置缓存
        if (umsMemberInfoDb != null) {
            jedis.setex("user:" + umsMember.getPassword() + ":info", 60 * 60 * 2, JSON.toJSONString(umsMemberInfoDb));

        }
        jedis.close();

        return umsMemberInfoDb;
    }

    @Override
    public void addUserToken(String token, String id) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + id + ":token", 60 * 60 * 2, token);
        jedis.close();
    }

    @Override
    public void addOauthUser(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsMember) {
        UmsMember umsMember1 = userMapper.selectOne(umsMember);
        return umsMember1;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String deliveryAddressId) {
        UmsMemberReceiveAddress address = new UmsMemberReceiveAddress();
        address.setId(deliveryAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress = umsMemberReceiveAddressMapper.selectOne(address);
        return umsMemberReceiveAddress;
    }

    /**
     * 获取所有用户信息
     *
     * @return 用户信息列表
     */
    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> umsMemberList = userMapper.selectAll();//userMapper.selectAllUser();

        return umsMemberList;
    }

    /**
     * 通过memberId查询收货人地址信息
     *
     * @param memberId 用户的成员id
     * @return 用户信息列表
     */
    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

 /*
        Example e = new Example(UmsMemberReceiveAddress.class);

        e.createCriteria().andEqualTo("memberId",memberId);

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(e);
 */
        //封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        //调用通用的Mapper工具类查询
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }


}
