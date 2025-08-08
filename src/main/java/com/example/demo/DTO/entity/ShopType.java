package com.example.demo.DTO.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_shop_type")
public class ShopType {

    @TableId(type = IdType.AUTO)
    private Long Id;
    private String type;
    private String isValid;
    private Date createTime;
    private Date updateTime;
}
