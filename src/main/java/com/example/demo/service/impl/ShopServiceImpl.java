package com.example.demo.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.Shop;
import com.example.demo.Mapper.ShopMapper;
import com.example.demo.service.ShopService;
import com.example.demo.utils.CacheClient;
import com.example.demo.utils.RedisCache;
import com.example.demo.utils.RedisData;
import com.github.pagehelper.util.StringUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.demo.utils.RedisConstatns.*;
import static com.example.demo.utils.ThreadPoolConfig.CACHE_REBUILD_EXECUTOR;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    RedisCache redisCache;

    @Resource
    CacheClient cacheClient;


    @Override
    public Result insertShop(Shop shop) {

        shop.setShopId("shopId_"+UUID.randomUUID().toString(true));
        shop.setCreateTime(new Date());
        save(shop);

        return Result.ok();
    }

    @Override
    public Result queryShopById(String shopId) {

        //1.原始做法
        //Shop shop = queryByShopId(shopId);

        //1.缓存穿透
        //Shop shop = queryWithPassThrough(shopId);
        Shop shop = cacheClient.queryWithPassThrough(SHOP_ID_KEY,
                shopId, Shop.class, this::selectByShopId, SHOP_ID_TTL, TimeUnit.MINUTES);

        //用互斥锁，解决缓存击穿
        //Shop shop = queryWithMutex(shopId);

        //逻辑过期，解决缓存击穿
        //Shop shop = queryWithLogicalExpire(shopId);
//        Shop shop = cacheClient.queryWithLogicalExpire(SHOP_ID_KEY, shopId, Shop.class,
//                this::selectByShopId, 20L, TimeUnit.SECONDS);

        if(shop == null) {
            return Result.fail("店铺信息不存在！");
        }
        //7.返回
        return Result.ok(shop);
    }


    /**
     * 逻辑过期,解决缓存穿透方法
     * @param shopId
     * @return
     */
    public Shop queryWithLogicalExpire(String shopId) {
        //1.从redis查询
        String shopKey = SHOP_ID_KEY+shopId;
        String cacheShop = redisCache.getCacheObject(shopKey);
        //2.判断redis是否存在
        if(StringUtil.isEmpty(cacheShop)){
            //3.不存在，直接返回
            return null;
        }

        //4.redis存在，把json反序列成对象
        RedisData redisData = JSONUtil.toBean(cacheShop, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        //5.判断逻辑过期
        if(expireTime.isAfter(LocalDateTime.now())) {
            //5.1未过期，返回店铺信息
            return shop;
        }

        //5.2过期，需要缓存重建
        //6.获取互斥锁
        String shopLockKey = SHOP_LOCK_KEY+shopId;
        boolean isLock = redisCache.tryLock(shopLockKey, "LOCK");

        if(isLock) {
            // TODO 6.1 锁获取成功，开启独立线程重建缓存
            CACHE_REBUILD_EXECUTOR.submit(() -> {

                try{
                    this.saveShop2Redis(shopId, 20L);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }finally {
                    //释放锁
                    redisCache.deleteObject(shopLockKey);
                }
            });
        }

        //6.2锁获取失败，返回旧的店铺信息
        return shop;
    }


    /**
     * 原始做法
     * @param shopId
     * @return
     */
    public Shop queryByShopId(String shopId) {
        //1.从redis查询
        String shopKey = SHOP_ID_KEY+shopId;
        String cacheShop = redisCache.getCacheObject(shopKey);
        //2.判断redis是否存在
        if(StringUtil.isNotEmpty(cacheShop)){
            //3.存在，直接返回
            return JSONUtil.toBean(cacheShop, Shop.class);
        }


        //4.不存在，查询数据库
        Shop shop = getById(shopId);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(shop == null){
            //5.不存在，返回错误
            return null;
        }

        //6.存在，写redis
        redisCache.setCacheObject(shopKey, JSONUtil.toJsonStr(shop), SHOP_ID_TTL, TimeUnit.MINUTES);
        //7.返回
        return shop;
    }



    /**
     * 解决缓存穿透方法
     * @param shopId
     * @return
     */
    public Shop queryWithPassThrough(String shopId) {
        //1.从redis查询
        String shopKey = SHOP_ID_KEY+shopId;
        String cacheShop = redisCache.getCacheObject(shopKey);
        //2.判断redis是否存在
        if(StringUtil.isNotEmpty(cacheShop)){
            //3.存在，直接返回
            return JSONUtil.toBean(cacheShop, Shop.class);
        }

        //判断redis命中的是否是空值
        if(cacheShop != null) {
            return null;
        }

        //4.不存在，查询数据库
        Shop shop = getById(shopId);

        if(shop == null){
            //4.1将空值写入redis，解决缓存穿透
            redisCache.setCacheObject(shopKey, "", SHOP_NULL_TTL, TimeUnit.MINUTES);
            //5.不存在，返回错误
            return null;
        }

        //6.存在，写redis
        redisCache.setCacheObject(shopKey, JSONUtil.toJsonStr(shop), SHOP_ID_TTL, TimeUnit.MINUTES);
        //7.返回
        return shop;
    }

    /**
     * 用互斥锁解决缓存击穿方法
     * @param shopId
     * @return
     */
    public Shop queryWithMutex(String shopId) {
        //1.从redis查询
        String shopKey = SHOP_ID_KEY+shopId;
        String cacheShop = redisCache.getCacheObject(shopKey);
        //2.判断redis是否存在
        if(StringUtil.isNotEmpty(cacheShop)){
            //3.存在，直接返回
            return JSONUtil.toBean(cacheShop, Shop.class);
        }

        //判断redis命中的是否是空值
        if(cacheShop != null) {
            return null;
        }

        //4.不存在，查询数据库
        //4.1实现缓存重建
        //4.1.1获取互斥锁，如果获取失败则休眠并重试
        String shopLockKey = null;
        Shop shop = null;
        try {
            shopLockKey = SHOP_LOCK_KEY+shopId;
            boolean isLock = redisCache.tryLock(shopLockKey, "LOCK");

            if(!isLock) {
                //获取失败，休眠
                Thread.sleep(50);
                return queryWithMutex(shopId);
            }

            //获取成功，则查询数据库
            shop = getById(shopId);
            Thread.sleep(200);
            if(shop == null){
                //4.1将空值写入redis，解决缓存穿透
                redisCache.setCacheObject(shopKey, "", SHOP_NULL_TTL, TimeUnit.MINUTES);
                //5.不存在，返回错误
                return null;
            }

            //6.存在，写redis
            redisCache.setCacheObject(shopKey, JSONUtil.toJsonStr(shop), SHOP_ID_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //7.释放互斥锁
            redisCache.deleteObject(shopLockKey);
        }
        //7.返回
        return shop;
    }

    /**
     * 保存shop到redis
     * @param shopId
     */
    public void saveShop2Redis(String shopId, Long expireSeconds) throws InterruptedException {
        //1.查询数据库
        Shop shop = getById(shopId);
        Thread.sleep(200L);
        String shopKey = SHOP_ID_KEY+shopId;
        //2.封装逻辑过期
        RedisData redisData = new RedisData();
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        redisData.setData(shop);

        //3.存redis
        redisCache.setCacheObject(shopKey, JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public Result updateByShopId(Shop shop) {
        String shopId = shop.getShopId();
        if(shopId == null) {
            return Result.fail("商户ID不能为空");
        }

        //1.更新数据库
        shop.setUpdateTime(new Date());
        updateById(shop);

        //2.删除redis缓存
        String shopKey = SHOP_ID_KEY+shop.getShopId();
        redisCache.deleteObject(shopKey);

        return Result.ok();
    }

    @Override
    public Shop selectByShopId(String shopId) {
        return getById(shopId);
    }
}
