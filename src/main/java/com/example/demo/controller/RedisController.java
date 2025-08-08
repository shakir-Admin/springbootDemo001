package com.example.demo.controller;

import com.example.demo.utils.RedisCache;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/redis")
public class RedisController {


    private final RedisCache redisCache;

    @RequestMapping("/setTest")
    public String setRedisTest(){
        redisCache.setCacheObject("age",20);
        //String name = redisTemplate.opsForValue().get("name").toString();
        //return name;
        return "success";
    }
}
