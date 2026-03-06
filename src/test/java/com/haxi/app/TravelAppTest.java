package com.haxi.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TravelAppTest {

    @Resource
    private TravelApp travelApp;

    @Test
    void testChat() {
        String chatID = UUID.randomUUID().toString();
        //第一轮
        String message = "你好我是章鱼哥";
        String answer = travelApp.doChat(message, chatID);
        Assertions.assertNotNull(answer);
        
        //第二轮
        message = "我想去海滩放松一下";
        answer = travelApp.doChat(message, chatID);
        Assertions.assertNotNull(answer);
        
        //第三轮
        message = "我想去哪里放松来着，你帮我回忆一下";
        answer = travelApp.doChat(message, chatID);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithReport() {
        String chatID = UUID.randomUUID().toString();
        String message = "你好,我是海绵宝宝，我想去海边玩，但我不知道哪里的海景好看";
        TravelApp.TravelReport travelReport = travelApp.doChatWithReport(message, chatID);
        Assertions.assertNotNull(travelReport);
    }

    @Test
    void doChatWithRag() {
        String chatID = UUID.randomUUID().toString();
        String message = "你好，我想临时来场自由行，但我不知道是否来得及现场买票";
        String answer = travelApp.doChatWithRag(message, chatID);
        Assertions.assertNotNull(answer);
    }
}