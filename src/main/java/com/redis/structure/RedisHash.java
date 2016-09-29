package com.redis.structure;

import com.alibaba.fastjson.JSON;
import com.redis.RedisPoolManager;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 谢俊权
 * @create 2016/5/18 16:57
 */
public class RedisHash<V> {

    private String key;
    private int expiredSeconds;
    private ConcurrentMap<String, V> map = new ConcurrentHashMap<String, V>();
    private RedisType<V> redisType;

    protected RedisHash(String key, RedisType<V> redisType) {
        this(key, 60 * 60, redisType);
    }

    protected RedisHash(String key, int expiredSeconds, RedisType<V> redisType) {
        this.key = key;
        this.expiredSeconds = expiredSeconds;
        this.redisType = redisType;
    }

    public RedisHash set(String field, V value){
        if(null != value) {
            map.put(field, value);
        }
        return this;
    }

    public RedisHash setAll(Map<String, V> values){
        if(!(null == values && values.isEmpty())){
            map.putAll(values);
        }
        return this;
    }

    public void syncSet(String field, V value){
        this.set(field, value).sync();
    }

    public void syncSetAll(Map<String, V> values){
        this.setAll(values).sync();
    }

    public void sync(){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        try {
            String script =
                    "local key = KEYS[1]\n" +
                            "local expire = KEYS[2]\n" +
                            "for i=1, #ARGV, 2 do\n" +
                            "\tlocal field = ARGV[i]\n" +
                            "\tlocal value = ARGV[i+1]\n" +
                            "\tredis.call(\"hset\", key, field, value)\n" +
                            "end\n" +
                            "local ttl = redis.call(\"ttl\", key)\n" +
                            "if ttl < 0 then\n" +
                            "\tredis.call(\"expire\", key, expire)\n" +
                            "end\n";
            jedis.eval(
                    script,
                    Arrays.asList(key, String.valueOf(expiredSeconds)),
                    Arrays.asList(toStringArray(map))
            );

        } finally {
            map.clear();
            jedis.close();
        }
    }

    public Map<String, V> get(String... fields){
        if(fields.length > 0){
            return getFromFields(fields);
        }
        return getAll();
    }

    private Map<String, V> getFromFields(String... fields){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        List<String> values = null;
        try {
            values = jedis.hmget(key, fields);
        } finally {
            jedis.close();
        }
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < fields.length; i++) {
            String key = fields[i];
            String value = values.get(i);
            map.put(key, value);
        }
        return toObjectMap(map);
    }

    private Map<String, V> getAll(){
        Jedis jedis = RedisPoolManager.getInstance().getSource();
        Map<String, String> map = null;
        try {
            map = jedis.hgetAll(key);
        } finally {
            jedis.close();
        }
        return toObjectMap(map);
    }



    private String[] toStringArray(Map<String, V> map){
        String[] result = new String[map.size()*2];
        Set<String> keySet = map.keySet();
        Iterator<String> it = keySet.iterator();
        int index = 0;
        while(it.hasNext()){
            String key = it.next();
            V value = map.get(key);
            result[index++] = key;
            result[index++] = JSON.toJSONString(value);
        }
        return result;
    }

    private Map<String, String> toStringHash(Map<String, V> map){
        Map<String, String> result = new HashMap<String, String>();
        Set<String> keySet = map.keySet();
        Iterator<String> it = keySet.iterator();
        while(it.hasNext()){
            String key = it.next();
            V value = map.get(key);
            result.put(key, JSON.toJSONString(value));
        }
        return result;
    }

    private V toObject(String value){
        V result = null;
        if(value != null){
            result = JSON.parseObject(value, this.redisType.actualType);
        }
        return result;
    }

    private Map<String, V> toObjectMap(Map<String, String> map){
        Map<String, V> result = new HashMap<String, V>();
        if(map != null){
            Set<String> keySet = map.keySet();
            Iterator<String> it = keySet.iterator();
            while(it.hasNext()){
                String key = it.next();
                String value = map.get(key);
                result.put(key, toObject(value));
            }
        }
        return result;
    }


}
