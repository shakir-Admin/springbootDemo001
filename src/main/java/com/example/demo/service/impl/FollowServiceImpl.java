package com.example.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.OV.UserDTO;
import com.example.demo.DTO.entity.Follow;
import com.example.demo.Mapper.FollowMapper;
import com.example.demo.service.FollowService;
import com.example.demo.service.UserService;
import com.example.demo.utils.UserHoler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    UserService userService;

    @Override
    public Result follow(String followUserId, Boolean isFollow) {
        UserDTO userDTO = UserHoler.getUser();
        if(userDTO == null){
            return Result.fail("为获取到当前用户信息！");
        }
        String userId = userDTO.getUserId();
        String key = "follows:"+userId;
        //判断是关注还是取消关注
        if(isFollow) {
            //关注则插入数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            follow.setCreateTime(LocalDateTime.now());

            boolean added = save(follow);
            if(added) {
                //数据库保存成功，保存redis
                stringRedisTemplate.opsForSet().add(key, followUserId);
            }
        } else {
            //取消关注则删除数据
            boolean removed = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));

            if(removed) {
                //数据库删除成功，删除redis
                stringRedisTemplate.opsForSet().remove(key, followUserId);
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(String followUserId) {
        UserDTO userDTO = UserHoler.getUser();
        if(userDTO == null){
            return Result.fail("未获取到当前用户信息！");
        }
        String userId = userDTO.getUserId();
        Long count = lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId)
                .count();

        return Result.ok(count>0);
    }

    @Override
    public Result followCommons(String id) {
        //获取当前用户
        UserDTO userDTO = UserHoler.getUser();
        if(userDTO == null){
            return Result.fail("未获取到当前用户信息！");
        }
        String userId = userDTO.getUserId();

        String key = "follows:"+userId;
        String otherKey = "follows:"+id;

        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, otherKey);
        if(intersect==null || intersect.isEmpty()){
            //无交集
            return Result.ok(Collections.emptyList());
        }
        List<String> userIds = intersect.stream().map(String::valueOf).collect(Collectors.toList());
        //3.根据用户id查询用户

        List<UserDTO> userDTOList = userService.query()
                .in("user_id", userIds).list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());


        return Result.ok(userDTOList);
    }

}
