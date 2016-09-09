package com.redis.structure;

/**
 * @author 谢俊权
 * @create 2016/9/9 10:25
 */
public class Redis {

    public static RedisList list(String key, RedisType type){
        return new RedisList(key, type);
    }

    public static RedisList list(String key, Class clazz){
        return new RedisList(key, new RedisType(clazz));
    }

    public static RedisSet set(String key, RedisType type){
        return new RedisSet(key, type);
    }

    public static RedisSet set(String key, Class clazz){
        return new RedisSet(key, new RedisType(clazz));
    }

    public static RedisHash hash(String key, RedisType type){
        return new RedisHash(key, type);
    }

    public static RedisHash hash(String key, Class clazz){
        return new RedisHash(key, new RedisType(clazz));
    }
}
