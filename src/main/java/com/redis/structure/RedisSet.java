package com.redis.structure;

import com.alibaba.fastjson.JSON;
import com.redis.RedisPoolManager;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
        try {
            String script =
                    "local key = KEYS[1]\n" +
                            "local expire = KEYS[2]\n" +
                            "for index, value in pairs(ARGV) do\n" +
                            "\tredis.call(\"sadd\", key, value)\n" +
                            "end\n" +
                            "local ttl = redis.call(\"ttl\", key)\n" +
                            "if ttl < 0 then\n" +
                            "\tredis.call(\"expire\", key, expire)\n" +
                            "end\n";
            jedis.eval(
                    script,
                    Arrays.asList(key, String.valueOf(expiredSeconds)),
                    Arrays.asList(toStringArray(set))
            );
        } finally {
            set.clear();
            jedis.close();
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
