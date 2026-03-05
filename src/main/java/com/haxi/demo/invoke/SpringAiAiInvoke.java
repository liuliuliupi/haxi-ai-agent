package com.haxi.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * spring AI框架调用大模型
 */
@Component
public class SpringAiAiInvoke implements CommandLineRunner {
    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String...args) throws Exception {
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你好我是派大星"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }

}
