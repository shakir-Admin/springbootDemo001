package com.example.demo.utils;

import cn.hutool.core.util.BooleanUtil;
import com.aliyuncs.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.demo.utils.RedisConstatns.SHOP_LOCK_TTL;


@Component
public class RedisCache {

    @Autowired
    public RedisTemplate redisTemplate;

    //@Autowired
    //StringRedisTemplate stringRedisTemplate;

    /**
     * 缓存基本对象，String,Integer,Object
     * @param key
     * @param value
     * @param <T>
     */
    public <T> void setCacheObject(final String key, final T value){
        redisTemplate.opsForValue().set(key, value);
        //stringRedisTemplate.opsForValue().set(key, value.toString());
    }


    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public Boolean hasKey(String key){
        return redisTemplate.hasKey(key);
    }

    /**
     * 获取缓存基本对象
     * @param key
     * @return
     * @param <T>
     */
    public <T> T getCacheObject(final String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        ValueOperations<String, T> operations = redisTemplate.opsForValue();
        return operations.get(key);
    }

    /**
     * 缓存List集合
     * @param key
     * @param dataList
     * @return
     * @param <T>
     */
    public <T> long setCacheList(final String key, final List<T> dataList){
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获取缓存的List集合
     * @param key
     * @return
     * @param <T>
     */
    public <T> List<T> getCacheList(final String key){
        return redisTemplate.opsForList().range(key,0,-1);
    }

    public <T> void setCacheObject(final String key, final T value, final Long ttl, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
    }

    /**
     * 存MAp
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if(dataMap != null){
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 从map中取
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 设置有效期
     */
    public boolean expire(final String key, final Long ttl, final TimeUnit timeUnit ) {
        return redisTemplate.expire(key, ttl,timeUnit);
    }

    /**
     * 删除单个对象
     */

    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     */
    public boolean deleteObject(final Collection collection){
        return redisTemplate.delete(collection) > 0;
    }

    /**
     * 获取锁
     * @param key
     * @param value
     * @return
     */
    public boolean tryLock(final String key, String value){
        Boolean lockFlag = redisTemplate.opsForValue().setIfAbsent(key, value, SHOP_LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(lockFlag);
    }



}
