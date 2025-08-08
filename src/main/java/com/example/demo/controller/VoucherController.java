package com.example.demo.controller;

import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.Voucher;
import com.example.demo.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/voucher")
public class VoucherController {

    private static final Logger logger = LoggerFactory.getLogger(VoucherController.class);

    final VoucherService voucherService;

    @PostMapping("addVoucher")
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.addVoucher(voucher);
        return Result.ok();
    }

}
