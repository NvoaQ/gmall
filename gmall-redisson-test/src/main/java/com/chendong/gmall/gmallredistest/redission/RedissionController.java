package com.chendong.gmall.gmallredistest.redission;

import com.chendong.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
public class RedissionController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redission;

    @RequestMapping("testRedisson")
    @ResponseBody
    public String testRedisson(){
        Jedis jedis = redisUtil.getJedis();

        //声明redisson的可重入锁，redisson实现了JUC包下的Lock
        RLock lock = redission.getLock("lock");

        //加锁
        lock.lock();
        try{
            //功能：k:1 k:2 k:3 ......
            String v = jedis.get("k");
            if(StringUtils.isBlank(v)){
                v="1";
            }
            System.out.println("->->"+v+"<-<-");
            jedis.set("k",(Integer.parseInt(v)+1)+"");
        }finally {
            jedis.close();
            //解锁
            lock.unlock();
        }

        return "success";
    }
}
