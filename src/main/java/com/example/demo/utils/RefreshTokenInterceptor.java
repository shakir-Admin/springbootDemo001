package com.example.demo.utils;

import cn.hutool.core.bean.BeanUtil;
import com.example.demo.DTO.OV.UserDTO;
import com.github.pagehelper.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.demo.utils.RedisConstatns.LOGIN_USER_KEY;
import static com.example.demo.utils.RedisConstatns.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenInterceptor.class);

    private RedisCache redisCache;

    public RefreshTokenInterceptor(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //logger.info("进入拦截器，将用户保存到ThreaLocal");
        //1.获取请求头的token
        String token = request.getHeader("Authorization");
        if(StringUtil.isEmpty(token)) {
            return true;
        }
        //1.获取session
        //HttpSession session = request.getSession();

        //2.从session中获取用户
        //Object user = session.getAttribute("user");
        //2.从redis中获取用户
        String cacheMapKey = LOGIN_USER_KEY+token;
        Map<String, Object> userMap = redisCache.getCacheMap(cacheMapKey);

        //3.判断用户是否存在
        if(userMap.isEmpty()){
            return true;
        }

        //5.将map转为对象
        UserDTO user = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //6.存在，保存到ThreaLocal
        UserHoler.saveUser((UserDTO) user);

        //7.刷新token有效期
        redisCache.expire(cacheMapKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        //8.放行
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //logger.info("进入拦截器销毁用户");
        //移除user
        UserHoler.removeUser();
    }
}
