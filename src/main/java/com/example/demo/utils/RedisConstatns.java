package com.example.demo.utils;

public class RedisConstatns {

    /**
     * redis在存储登录验证码的key
     */
    public static final String LOGIN_CODE_KEY = "login:code:";
    /**
     * redis在存储登录验证码的有效期
     */
    public static final Long LOGIN_CODE_TTL = 2L;
    /**
     * redis在存储用户的key
     */
    public static final String LOGIN_USER_KEY = "login:token:";

    /**
     * redis在存储用户的有效期
     */
    public static final Long LOGIN_USER_TTL = 30L;

    /**
     * redis在存储商户信息的key
     */
    public static final String SHOP_ID_KEY = "shop:shopId:";
    /**
     * redis在存储商户信息的有效期
     */
    public static final Long SHOP_ID_TTL = 30L;

    /**
     * redis在存储商户类型的key
     */
    public static final String SHOP_TYPE_KEY = "shop:shopType:";
    /**
     * redis在存储商户类型的有效期
     */
    public static final Long SHOP_TYPE_TTL = 30L;


    /**
     * redis在存储商户信息(空值)的有效期
     */
    public static final Long SHOP_NULL_TTL = 2L;

    /**
     * redis获取reid锁的key
     */
    public static final String SHOP_LOCK_KEY = "shop:lock:";

    /**
     * redis获取reid锁的时常
     */
    public static final Long SHOP_LOCK_TTL = 10L;

    /**
     * redis 优惠券库存key
     */
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";

    /**
     * redis 笔记点赞key
     */
    public static final String BLOG_LIKE_KEY = "blog:like:";

    /**
     * redis 笔记推送key
     */
    public static final String FEED_KEY = "feed:";

}
