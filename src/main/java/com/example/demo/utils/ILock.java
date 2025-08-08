package com.example.demo.utils;

public interface ILock {

    /**
     *
     * 尝试获取锁
     * @param timeoutSec 锁持有的时间，过期后自动释放锁
     * @return true：获取锁成功；false:获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
