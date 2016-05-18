package com.bj.redis.structure;

import com.bj.redis.RedisPoolManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/5/18 16:57
 */
public class RedisList {

    public static class InternalList{

        private Jedis jedis = RedisPoolManager.getInstance().getSource();
        private Pipeline pipeline;
        private String key;

        public InternalList(String key) {
            this.key = key;
            this.pipeline = jedis.pipelined();
        }

        public InternalList add(String value){
            if(null != value) {
                this.pipeline.lpush(this.key, value);
            }
            return this;
        }

        public InternalList addAll(List<String> values){
            if(!(null == values && values.isEmpty())){
                this.pipeline.lpush(this.key, values.toArray(new String[values.size()]));
            }
            return this;
        }

        public void syncAdd(String value){
            this.add(value).sync();
        }

        public void sync(){
            try {
                this.pipeline.sync();
            } finally {
                try {
                    this.pipeline.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.jedis.close();
            }
        }

        public List<Object> syncAndReturn(){
            try {
                return this.pipeline.syncAndReturnAll();
            } finally {
                try {
                    this.pipeline.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.jedis.close();
            }
        }

    }

    public static InternalList key(String key){
        return new InternalList(key);
    }


}
