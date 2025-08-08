package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.VoucherOrder;
import com.example.demo.Mapper.VoucherOrderMapper;
import com.example.demo.service.SeckillVoucherService;
import com.example.demo.service.VoucherOrderService;
import com.example.demo.utils.RedisIdWorker;
import com.example.demo.utils.UserHoler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    private ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                //1.获取队列中的订单信息
                try {
                    //1.获取队列中的订单信息
                    VoucherOrder voucherOrder = orderTasks.take();

                    //2.创建订单
                    handleVoucherOrder(voucherOrder);

                } catch (Exception e) {
                    log.error("处理订单异常", e);
                }
            }
        }
    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        String userId = voucherOrder.getUserId();

        //创建Redisson锁
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        //获取锁
        boolean isLock = lock.tryLock();
        if(!isLock){
            log.error("不允许重复下单！");
        }
        try {
            proxy.createVoucherOrder(voucherOrder);
            //}
        }finally {
            lock.unlock();
        }
    }

    private VoucherOrderService proxy;

    @Override
    public Result seckillVoucher(Long voucherId) {

        String userId = UserHoler.getUser().getUserId();

        //1.执行lua脚本
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId);


        //2.判断结果是否为0
        int r = result.intValue();
        if(r != 0){
            return Result.fail(r == 1 ? "库存不足！" : "不能重复下单！");
        }

        long orderId = redisIdWorker.nextId("order");
        //3.把下单信息保存到阻塞队列
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);

        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setPayType(1);
        voucherOrder.setStatus(1);
        voucherOrder.setCreateTime(new Date());

        orderTasks.add(voucherOrder);

        //获取代理对象（事务）
        proxy = (VoucherOrderService) AopContext.currentProxy();
        return Result.ok(orderId);
    }

//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        //1.查询优惠券
//        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
//        //2.判断秒杀是否开始
//        if(seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())){
//            //秒杀开始时间在当前时间之后，秒杀尚未开始
//            return Result.fail("秒杀尚未开始!");
//        }
//        //3.判断秒杀是否结束
//        if(seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
//            //秒杀结束时间在当前时间之前，秒杀已结束
//            return Result.fail("秒杀活动已结束！");
//        }
//        //4.判断库存是否充足
//        if(seckillVoucher.getStock()<1) {
//            return Result.fail("库存不足！");
//        }
//
//        String userId = UserHoler.getUser().getUserId();
//        //7.返回订单id
//        //synchronized (userId.intern()) {
//
//        //创建锁对象
//        //SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
//
//        //创建Redisson锁
//        RLock lock = redissonClient.getLock("lock:order:" + userId);
//        //获取锁
//        boolean isLock = lock.tryLock();
//        if(!isLock){
//            return Result.fail("不允许重复下单！");
//        }
//        try {
//            //获取代理对象（事务）
//            VoucherOrderService proxy = (VoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//            //}
//        }finally {
//            lock.unlock();
//        }
//    }

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        //5.一人一单判断
        String userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        //5.1查询订单
        Long count = lambdaQuery()
                .eq(VoucherOrder::getVoucherId, voucherId)
                .eq(VoucherOrder::getUserId, userId)
                .count();
        //5.2判断是否存在
        if(count > 0) {
            log.error("该用户已购买一次！");
        }

        //6.扣减库存
        boolean i = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if(i) {
            log.error("库存不足！");
        }

        //7.创建订单
//        VoucherOrder voucherOrder = new VoucherOrder();
//        Long orderId = redisIdWorker.nextId("order");
//        voucherOrder.setId(orderId);
//
//        voucherOrder.setUserId(userId);
//        voucherOrder.setVoucherId(voucherId);
//        voucherOrder.setPayType(1);
//        voucherOrder.setStatus(1);
//        voucherOrder.setCreateTime(new Date());
        save(voucherOrder);

        //return Result.ok(voucherOrder.getId());
    }
}
