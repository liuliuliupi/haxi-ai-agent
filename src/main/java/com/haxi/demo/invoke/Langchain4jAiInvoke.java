package com.haxi.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;

public class Langchain4jAiInvoke {

    public static void main(String[] args) {
        // 从环境变量读取 API Key（仅用于测试）
        String apiKey = System.getenv("DASHSCOPE_API_KEY");

        ChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen-plus")
                .build();

        String answer = qwenChatModel.chat("我是派大星");
        System.out.println(answer);
    }
}
