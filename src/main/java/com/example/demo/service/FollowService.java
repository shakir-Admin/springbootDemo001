package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.Follow;

import java.util.List;

public interface FollowService extends IService<Follow> {
    Result follow(String followUserId, Boolean isFollow);

    Result isFollow(String followUserId);

    Result followCommons(String id);

}
