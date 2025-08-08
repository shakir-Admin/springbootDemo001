package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.DTO.OV.LoginFormDTO;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.entity.User;
import jakarta.servlet.http.HttpSession;


public interface UserService extends IService<User> {

    User findById(String userId);

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginFormDTO, HttpSession session);

}
