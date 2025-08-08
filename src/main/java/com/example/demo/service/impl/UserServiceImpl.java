package com.example.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.DTO.OV.LoginFormDTO;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.OV.UserDTO;
import com.example.demo.DTO.entity.User;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.utils.RedisCache;
import com.example.demo.utils.RegexUtils;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.demo.utils.RedisConstatns.*;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    RedisCache redisCache;

    @Override
    public User findById(String userId) {
        User user = query().eq("user_id", userId).one();
        redisCache.setCacheObject(String.valueOf(userId), user);
        return user;

    }

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号
        if(!RegexUtils.isPhoneLegal(phone)) {
            //2.如果不符合，返回错误信息
            return Result.fail("手机号码格式错误！");
        }

        //3.生成验证码
        String code = RandomUtil.randomNumbers(6);

        //4.保存验证码到session
        //session.setAttribute("code", code);
        redisCache.setCacheObject(LOGIN_CODE_KEY+phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //5.发送验证码
        logger.info("发送短信验证码：{}", code);

        //6.返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginFormDTO, HttpSession session) {
        //1.校验手机号
        if(!RegexUtils.isPhoneLegal(loginFormDTO.getPhone())) {
            //2.如果不符合，返回错误信息
            return Result.fail("手机号码格式错误！");
        }
        //2.校验验证码

        //Object cacheCode = session.getAttribute("code");
        Object cacheCode = redisCache.getCacheObject(LOGIN_CODE_KEY+loginFormDTO.getPhone());
        String code = loginFormDTO.getCode();
        if(cacheCode == null || !cacheCode.toString().equals(code)){
            //3.不一致，报错
            return Result.fail("验证码无效！");
        }
        //4.一致，根据手机号查询用户
        User user = query().eq("user_phone", loginFormDTO.getPhone()).one();
        //5.判断用户是否存在
        if(user == null){
            //6.不存在，新建用户并保存
            user = createUserWithPhone(loginFormDTO.getPhone());
        }

        //7.保存用户信息到session
        //7.1随机生成一个token
        String token = UUID.randomUUID().toString(true);
        //7.2将user保存到hash中
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> cacheMap = BeanUtil.beanToMap(userDTO);

        String cacheMapKey = LOGIN_USER_KEY+token;
        redisCache.setCacheMap(cacheMapKey, cacheMap);
        redisCache.expire(cacheMapKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        //session.setAttribute("user", userDTO);

        //7.3返回token给前端
        return Result.ok(token);
    }


    private User createUserWithPhone(String phone) {
        //1.创建用户
        User user  = new User();
        user.setUserId("userId_"+UUID.randomUUID().toString(true));
        user.setUserPhone(phone);
        String userNick = "user_"+RandomUtil.randomString(15);
        user.setUserNick(userNick);
        user.setCreateTime(new Date());
        //2.保存用户
        save(user);
        return user;
    }
}
