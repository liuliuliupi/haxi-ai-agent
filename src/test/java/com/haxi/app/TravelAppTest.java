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
        String message = "你好，我想来场临时来场灵活自由行，但我不知道做哪些准备";
        String answer = travelApp.doChatWithRag(message, chatID);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("周末想带女朋友去青岛约会，推荐几个适合情侣的小众打卡地？");

        // 测试网页抓取：旅行案例分析
        testMessage("最近找工作太焦虑了，看看百度网站（www.baidu.com）的其他人都是去哪里散心的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的海滩图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的旅行档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘五一旅行计划’PDF，包含门票预订、规划行程和打卡清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = travelApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
//        String message = "我的驴友居住在青岛市黄岛区，请帮我找到 10 公里内合适的登山地点";
//        String answer =  travelApp.doChatWithMcp(message, chatId);
//        Assertions.assertNotNull(answer);
        // 测试图片搜索 MCP
        String message = "帮我搜索一些适合拍毕业照的风景图片";
        String answer =  travelApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }
}