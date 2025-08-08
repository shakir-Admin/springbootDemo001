package com.example.demo.DTO;


import lombok.Data;

@Data
public class ChatBody {

    //会话ID
    private String chatId;
    //会话类型
    private String type;
    //会话内容
    private String message;
}
