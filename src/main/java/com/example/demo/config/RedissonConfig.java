package com.example.demo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        //1.配置
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.137.130:6379")
                .setPassword("redis123");

        //2.创建对象
        return Redisson.create(config);
    }

}
