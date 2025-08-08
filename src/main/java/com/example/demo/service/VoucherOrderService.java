package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.VoucherOrder;

public interface VoucherOrderService extends IService<VoucherOrder> {
    Result seckillVoucher(Long voucherId);

    void createVoucherOrder(VoucherOrder voucherOrder);
}
