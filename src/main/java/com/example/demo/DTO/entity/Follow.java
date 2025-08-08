package com.example.demo.DTO.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_follow")
public class Follow {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String followUserId;
    private LocalDateTime createTime;
}
