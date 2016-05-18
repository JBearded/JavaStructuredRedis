package com.bj.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author 谢俊权
 * @create 2016/5/18 17:07
 */
public class RedisPoolManager {


    private JedisPool jedisPool;

    private static class RedisPoolManagerHolder{
        public static RedisPoolManager redisPoolManager = new RedisPoolManager();
    }
    private RedisPoolManager(){

    }
    public static RedisPoolManager getInstance(){
        return RedisPoolManagerHolder.redisPoolManager;
    }


    public void init(JedisPoolConfig config, String ip, int port, int timeout) {
        this.jedisPool = new JedisPool(config, ip, port, timeout);
    }

    public Jedis getSource(){
        return jedisPool.getResource();
    }
}
