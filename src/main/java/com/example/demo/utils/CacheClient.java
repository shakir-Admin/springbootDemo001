package com.example.demo.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.util.StringUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.example.demo.utils.RedisConstatns.SHOP_LOCK_KEY;
import static com.example.demo.utils.RedisConstatns.SHOP_NULL_TTL;
import static com.example.demo.utils.ThreadPoolConfig.CACHE_REBUILD_EXECUTOR;


@Slf4j
@Component
public class CacheClient {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        //设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));

        //写redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 解决缓存穿透方法
     * @param
     * @return
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix,
                                          ID id,
                                          Class<R> type,
                                          Function<ID, R> dbFallback,
                                          Long time,
                                          TimeUnit unit) {
        //1.从redis查询
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断redis是否存在
        if(StringUtil.isNotEmpty(json)){
            //3.存在，直接返回
            return JSONUtil.toBean(json, type);
        }

        //判断redis命中的是否是空值
        if(json != null) {
            return null;
        }

        //4.不存在，查询数据库
        R r = dbFallback.apply(id);
        if(r == null){
            //4.1将空值写入redis，解决缓存穿透
            stringRedisTemplate.opsForValue().set(key, "", SHOP_NULL_TTL, TimeUnit.MINUTES);
            //5.不存在，返回错误
            return null;
        }

        //6.存在，写redis
        this.set(key, r, time, unit);

        //7.返回
        return r;
    }


    /**
     * 逻辑过期,解决缓存穿透方法
     * @param
     * @return
     */
    public <R, ID> R queryWithLogicalExpire(String keyPrefix,
                                       ID id,
                                       Class<R> type,
                                            Function<ID, R> dbFallback,
                                            Long time,
                                            TimeUnit unit) {
        //1.从redis查询
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断redis是否存在
        if(StringUtil.isEmpty(json)){
            //3.不存在，直接返回
            return null;
        }

        //4.redis存在，把json反序列成对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //5.判断逻辑过期
        if(expireTime.isAfter(LocalDateTime.now())) {
            //5.1未过期，返回店铺信息
            return r;
        }

        //5.2过期，需要缓存重建
        //6.获取互斥锁
        String shopLockKey = SHOP_LOCK_KEY+id;

        //boolean isLock = redisCache.tryLock(shopLockKey, "LOCK");
        Boolean lockFlag = stringRedisTemplate.opsForValue().setIfAbsent(shopLockKey, "LOCK");
        if(BooleanUtil.isTrue(lockFlag)) {
            // TODO 6.1 锁获取成功，开启独立线程重建缓存
            CACHE_REBUILD_EXECUTOR.submit(() -> {

                try{
                    //1.先查数据库
                    R r1 = dbFallback.apply(id);
                    //2.写redis
                    this.setWithLogicalExpire(key, r1, time, unit);
                    //this.saveShop2Redis(shopId, 20L);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }finally {
                    //释放锁
                    stringRedisTemplate.delete(shopLockKey);
                    //redisCache.deleteObject(shopLockKey);
                }
            });
        }

        //6.2锁获取失败，返回旧的店铺信息
        return r;
    }

}
