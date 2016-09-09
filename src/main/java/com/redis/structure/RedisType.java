package com.redis.structure;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author 谢俊权
 * @create 2016/8/3 14:17
 */
public class RedisType<T> {

    public final Type actualType;

    protected RedisType() {
        Type superClass = this.getClass().getGenericSuperclass();
        this.actualType = ((ParameterizedType)superClass).getActualTypeArguments()[0];
    }

    protected RedisType(ParameterizedType type){
        this.actualType = type;
    }

    protected RedisType(Class type){
        this.actualType = type;
    }
}
