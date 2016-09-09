package com.redis.structure;

import com.alibaba.fastjson.JSON;
import com.redis.RedisPoolManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author 谢俊权
 * @create 2016/5/18 16:58
 */
public class RedisSet<T> {

    private String key;
    private int expiredSeconds;
    private Set<T> set = new CopyOnWriteArraySet<T>();
    private RedisType<T> redisType;

    protected RedisSet(String key, RedisType<T> redisType) {
        this(key, 60 * 60, redisType);
    }

    protected RedisSet(String key, int expiredSeconds, RedisType<T> redisType) {
        this.key = key;
        this.expiredSeconds = expiredSeconds;
        this.redisType = redisType;
    }

    public RedisSet add(T value){
        if(null != value) {
            set.add(value);
        }
        return this;
    }

    public RedisSet addAll(Set<T> values){
        if(!(null == values && values.isEmpty())){
            set.addAll(values);
        }
        return this;
    }

    public void syncAdd(T value){
        this.add(value).sync();
    }

    public void syncAddAll(Set<T> values){
        this.addAll(values).sync();
    }

    public void sync(){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        Pipeline pipeline = jedis.pipelined();
        try {
            pipeline.sadd(key, toStringArray(set));
            Response<Long> ttl = pipeline.ttl(key);
            pipeline.sync();
            if(ttl.get() < 0){
                jedis.expire(key, expiredSeconds);
            }
        } finally {
            set.clear();
            try {
                jedis.close();
                pipeline.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public T pop(){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        String value = null;
        try {
            value  = jedis.spop(key);
        } finally {
            jedis.close();
        }
        return toObject(value);
    }

    public Set<T> pop(int count){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        Set<String> set = null;
        try {
            set = jedis.spop(key, count);
        } finally {
            jedis.close();
        }
        return toObjectSet(set);
    }

    public Set<T> members(){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        Set<String> set = null;
        try {
            set = jedis.smembers(key);
        } finally {
            jedis.close();
        }
        return toObjectSet(set);
    }

    private String[] toStringArray(Set<T> set){
        String[] array = new String[set.size()];
        int index = 0;
        Iterator<T> it = set.iterator();
        while(it.hasNext()){
            T value = it.next();
            array[index++] = JSON.toJSONString(value);
        }
        return array;
    }

    private T toObject(String value){
        T result = null;
        if(value != null){
            result = JSON.parseObject(value, this.redisType.actualType);
        }
        return result;
    }

    private Set<T> toObjectSet(Set<String> set){
        Set<T> result = new HashSet<T>();
        if(set != null){
            Iterator<String> it = set.iterator();
            while(it.hasNext()){
                String value = it.next();
                result.add(toObject(value));
            }
        }
        return result;
    }


}
