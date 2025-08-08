package com.example.demo.DTO.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_blog")
public class Blog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String shopId;
    private String userId;
    private String title;
    private String images;
    private String content;
    private Integer liked;
    private Integer comments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    //用户图标
    @TableField(exist = false)
    private String icon;
    //用户名
    @TableField(exist = false)
    private String name;
    //是否点过赞
    @TableField(exist = false)
    private Boolean isLike;
}
