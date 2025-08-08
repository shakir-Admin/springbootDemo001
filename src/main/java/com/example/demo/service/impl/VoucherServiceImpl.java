package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.DTO.entity.SeckillVoucher;
import com.example.demo.DTO.entity.Voucher;
import com.example.demo.Mapper.VoucherMapper;
import com.example.demo.service.SeckillVoucherService;
import com.example.demo.service.VoucherService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.example.demo.utils.RedisConstatns.SECKILL_STOCK_KEY;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addVoucher(Voucher voucher) {
        save(voucher);

        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucher.setCreateTime(new Date());
        seckillVoucherService.save(seckillVoucher);

        //保存秒杀库存到redis
        stringRedisTemplate.opsForValue().
                set(SECKILL_STOCK_KEY+voucher.getId(),
                        voucher.getStock().toString());
    }
}
