package com.example.demo.DTO.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_voucher_order")
public class VoucherOrder {

    private Long id;
    private String userId;
    private Long voucherId;
    private int payType;
    private int status;
    private Date createTime;
    private Date payTime;
    private Date useTime;
    private Date refundTime;
    private Date updateTime;
}
