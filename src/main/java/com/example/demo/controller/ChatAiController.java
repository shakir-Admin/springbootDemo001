package com.example.demo.controller;

import com.example.demo.DTO.ChatBody;
import com.example.demo.repository.InMemoryChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;


@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatAiController {

    private final ChatClient chatClient;
    private final InMemoryChatHistoryRepository inMemoryChatHistoryRepository;

    @RequestMapping("/chat")
    public Flux<String> chat(@RequestBody ChatBody chatBody){
        //1.保存会话ID
        inMemoryChatHistoryRepository.saveChatId(chatBody.getType(), chatBody.getChatId());

        //2.请求模型
        Flux<String> content = chatClient
                .prompt()
                .user(chatBody.getMessage())
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatBody.getChatId()))
                .stream()
                .content();

        return content;
    }
}
