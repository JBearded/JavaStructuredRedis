package com.redis.structure;

import com.alibaba.fastjson.JSON;
import com.redis.RedisPoolManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author 谢俊权
 * @create 2016/5/18 16:57
 */
public class RedisList<T>{

    private String key;
    private int expiredSeconds;
    private List<T> list = new CopyOnWriteArrayList<T>();
    private RedisType<T> redisType;

    protected RedisList(String key, RedisType<T> redisType) {
        this(key, 60 * 60, redisType);
    }

    protected RedisList(String key, int expiredSeconds, RedisType<T> redisType) {
        this.key = key;
        this.expiredSeconds = expiredSeconds;
        this.redisType = redisType;
    }

    public RedisList push(T value){
        if(null != value) {
            list.add(value);
        }
        return this;
    }

    public RedisList pushAll(List<T> values){
        if(!(null == values && values.isEmpty())){
            list.addAll(values);
        }
        return this;
    }

    public void syncPush(T value){
        this.push(value).sync();
    }

    public void syncPushAll(List<T> values){
        this.pushAll(values).sync();
    }

    public List<T> range(int start, int end){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        List<String> list = null;
        try {
            list = jedis.lrange(key, start, end);
        } finally {
            jedis.close();
        }
        return toObjectList(list);
    }

    public T pop(){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        String value = null;
        try {
            value = jedis.lpop(key);
        } finally {
            jedis.close();
        }
        return toObject(value);
    }

    public void sync(){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        Pipeline pipeline = jedis.pipelined();
        try {
            pipeline.rpush(key, toStringArray(list));
            Response<Long> ttl = pipeline.ttl(key);
            pipeline.sync();
            if(ttl.get() < 0){
                jedis.expire(key, expiredSeconds);
            }
        } finally {
            list.clear();
            try {
                jedis.close();
                pipeline.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private String[] toStringArray(List<T> list){
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            T value = list.get(i);
            array[i] = JSON.toJSONString(value);
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
    
    private List<T> toObjectList(List<String> list){
        List<T> result = new ArrayList<T>();
        if(list != null){
            for (String value : list) {
                result.add(toObject(value));
            }
        }
        return result;
    }
}
