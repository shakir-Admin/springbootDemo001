package com.example.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.DTO.OV.LoginFormDTO;
import com.example.demo.DTO.OV.Result;
import com.example.demo.DTO.OV.UserDTO;
import com.example.demo.DTO.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.UserHoler;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    final UserService userService;

    @RequestMapping("/findAll")
    public List<User> findAllUser(){

        return userService.list();
    }

    @RequestMapping("/addUser")
    public Map<String, String> addUser(@RequestBody User user) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        String resultCode = "";
        String resultMsg = "";
        try{
            if(user != null){
                boolean i = userService.save(user);
                if(i){
                    resultCode = "00";
                    resultMsg = "保存成功";
                }else {
                    resultCode = "02";
                    resultMsg = "保存失败，数据库操作异常";
                }
            }else {
                resultCode = "01";
                resultMsg = "保存失败，user不能为空";
            }
        } catch (Exception e){
            resultCode = "99";
            resultMsg = "保存失败，异常";
        }

        resultMap.put("resultCode", resultCode);
        resultMap.put("resultMsg", resultMsg);
        return resultMap;
    }

    @RequestMapping("/findByUserId")
    public User findByUserId(@RequestParam(name = "userId") String userId){
        return userService.findById(userId);
    }


    @RequestMapping("/insertUser")
    public Map<String, String> insertUser(@RequestBody User user) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        String resultCode = "";
        String resultMsg = "";
        if(user != null){
            boolean i = userService.save(user);
            if(i){
                resultCode = "00";
                resultMsg = "保存成功";
            }else {
                resultCode = "02";
                resultMsg = "保存失败，数据库操作异常";
            }
        }else {
            resultCode = "01";
            resultMsg = "保存失败，user不能为空";
        }
        resultMap.put("resultCode", resultCode);
        resultMap.put("resultMsg", resultMsg);
        return resultMap;
    }

    @RequestMapping("/batchInsert")
    public Map<String, String> batchInsert(@RequestBody List<User> users){
        Map<String, String> resultMap = new HashMap<>();
        String resultCode = "";
        String resultMsg = "";
        if(users != null && users.size()>0){
            boolean i = userService.saveBatch(users);
            if(i){
                resultCode = "00";
                resultMsg = "保存成功";
            }else {
                resultCode = "02";
                resultMsg = "保存失败，数据库操作异常";
            }
        }else {
            resultCode = "01";
            resultMsg = "保存失败，user不能为空";
        }
        resultMap.put("resultCode", resultCode);
        resultMap.put("resultMsg", resultMsg);
        return resultMap;
    }

    @RequestMapping("/updateUser")
    public Map<String, String> updateUser(@RequestBody User user){
        Map<String, String> resultMap = new HashMap<>();
        String resultCode = "";
        String resultMsg = "";
        if(user != null){
            boolean u = userService.update(user, null);
            if(u){
                resultCode = "00";
                resultMsg = "保存成功";
            }else {
                resultCode = "02";
                resultMsg = "保存失败，数据库操作异常";
            }
        }else {
            resultCode = "01";
            resultMsg = "保存失败，user不能为空";
        }
        resultMap.put("resultCode", resultCode);
        resultMap.put("resultMsg", resultMsg);
        return resultMap;
    }


    /**
     * 发送短信验证码
     * @param phone
     * @param session
     * @return
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam(name = "phone") String phone, HttpSession session){
        return userService.sendCode(phone, session);
    }


    /**
     * 登录接口
     */
    @PostMapping("login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        return userService.login(loginFormDTO, session);
    }


    @PostMapping("me")
    public Result me(){
        UserDTO user = UserHoler.getUser();
        return Result.ok(user);
    }


//    @RequestMapping("/pageQuery")
//    public Page<User> PageQuery(@RequestParam(name = "pageNo") Integer pageNo
//            , @RequestParam(name = "pageSize") Integer pageSize){
//
//
//        Page<User> page = Page.of(pageNo, pageSize);
//        return userService.page(page);
//    }

}
