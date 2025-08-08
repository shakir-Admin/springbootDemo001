package com.example.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.OV.ScrollResult;
import com.example.demo.DTO.OV.UserDTO;
import com.example.demo.DTO.entity.Blog;
import com.example.demo.DTO.entity.Follow;
import com.example.demo.DTO.entity.User;
import com.example.demo.Mapper.BlogMapper;
import com.example.demo.service.BlogService;
import com.example.demo.service.FollowService;
import com.example.demo.service.UserService;
import com.example.demo.utils.UserHoler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.demo.utils.RedisConstatns.BLOG_LIKE_KEY;
import static com.example.demo.utils.RedisConstatns.FEED_KEY;

@Slf4j
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    BlogMapper blogMapper;

    @Resource
    UserService userService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FollowService followService;


    @Override
    public Result queryBlogById(Long id) {
        //1.查询blog
        Blog blog = getById(id);
        if(blog == null) {
            return Result.fail("笔记不存在！");
        }



        //2.查询blog有关用户
        String userId = blog.getUserId();
        User user = userService.findById(userId);
        blog.setName(user.getUserName());

        //3.查询blog是否被掉过赞
        isBlogLiked(blog);

        return Result.ok(blog);
    }

    @Override
    public Result likeBlog(Long id) {

        //1.获取当前用户
        String userId = UserHoler.getUser().getUserId();
        //2.判断当前用户是否点过赞
        String key = BLOG_LIKE_KEY+id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId);
        //3.如果未点过赞，数据库点赞加1，保存用户到redis集合
        if(score == null) {
            boolean updateFlag = update().setSql("liked = liked + 1").eq("id", id).update();
            if(updateFlag) {
                stringRedisTemplate.opsForZSet().add(key, userId, System.currentTimeMillis());
            }
        } else {
            //4.点过赞，取消点赞，数据库点赞-1，从redis集合中删除用户
            boolean updateFlag = update().setSql("liked = liked - 1").eq("id", id).update();
            if(updateFlag) {
                stringRedisTemplate.opsForZSet().remove(key, userId);
            }
        }

        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = BLOG_LIKE_KEY+id;
        //1.查询top5的点赞用户
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if(top5==null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        //2.解析出用户ID
        List<String> ids = top5.stream().map(String::valueOf).collect(Collectors.toList());

        //3.根据用户id查询用户
        String idStr = StrUtil.join(",", ids);

        List<UserDTO> userDTOS = userService.query()
                .in("user_id", ids)
                .last("ORDER BY FIELD(user_id, "+"'"+idStr+"'"+")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        //4.返回用户列表

        return Result.ok(userDTOS);
    }

    @Override
    public Result addBlog(Blog blog) {
        //1.获取当前用户
        String userId = UserHoler.getUser().getUserId();
        //2.保存笔记
        blog.setUserId(userId);
        blog.setLiked(0);
        blog.setCreateTime(LocalDateTime.now());
        boolean saved = save(blog);
        if(saved){
            return Result.fail("笔记新增失败！");
        }

        //3.查询笔记作者的粉丝

        List<Follow> follows = followService.query().eq("follow_user_id", userId).list();

        //4.推送笔记id给所有粉丝
        for (Follow follow : follows) {
            //循环推送到 redis
            String key = FEED_KEY+follow.getUserId();
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        //5.返回
        return Result.ok(blog.getId());

    }

    @Override
    public Result queryBlogOfFollow(Long lastId, Integer offset) {
        //1.获取当前用户
        UserDTO user = UserHoler.getUser();
        if (user == null) {
            return Result.fail("获取当前用户失败！");
        }
        String userId = user.getUserId();
        //2.从redis获取收件箱笔记
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, lastId, offset, 2);

        if(typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok();
        }

        //3.解析数据；blogId, minTime, offset
        List<Long> idList = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for(ZSetOperations.TypedTuple<String> tuple: typedTuples) {
            //3.1获取id
            idList.add(Long.valueOf(tuple.getValue()));
            //3.2获取时间戳
            long time = tuple.getScore().longValue();
            if(time == minTime){
                os ++;
            }else {
                minTime = time;
                os = 1;
            }
        }
        //4.根据笔记id查询blog
        List<Blog> blogListByIds = listByIds(idList);

        //5.返回
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(blogListByIds);
        scrollResult.setOffset(offset);
        scrollResult.setMinTime(minTime);
        return Result.ok(scrollResult);
    }

    private void isBlogLiked(Blog blog) {
        //1.获取当前用户

        UserDTO userDTO = UserHoler.getUser();
        if(userDTO == null) {
            //用户未登录
            return;
        }

        String userId = userDTO.getUserId();
        //2.判断当前用户是否点过赞
        String key = BLOG_LIKE_KEY+blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId);
        blog.setIsLike(score != null);
    }
}
