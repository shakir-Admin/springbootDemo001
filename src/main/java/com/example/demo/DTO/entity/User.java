package com.example.demo.DTO.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_user")
public class User {

    @TableField("user_id")
    private String userId;
    private String userNick;
    private String userName;
    private int userAge;
    private String userPhone;
    private String userPassword;
    private String userEmail;
    private Date createTime;
    private Date updateTime;
}
