package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.Blog;

public interface BlogService extends IService<Blog> {
    Result queryBlogById(Long id);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);

    Result addBlog(Blog blog);

    Result queryBlogOfFollow(Long lastId, Integer offset);
}
