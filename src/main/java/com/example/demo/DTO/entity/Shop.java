package com.example.demo.DTO.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_shop")
public class Shop {
    @TableId("shop_id")
    private String shopId;
    private String shopName;
    private String shopTelephone;
    private String shopType;
    private String shopAddress;
    private Date createTime;
    private Date updateTime;
}
