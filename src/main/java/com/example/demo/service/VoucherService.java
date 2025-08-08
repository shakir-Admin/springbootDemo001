package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.DTO.entity.Voucher;

public interface VoucherService extends IService<Voucher> {

    void addVoucher(Voucher voucher);
}
