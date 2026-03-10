package com.haxi.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HaxiManusTest {
    @Resource
    private HaxiManus haxiManus;

    @Test
    public void run() {
        String userPrompt = """
                我的驴友居住在青岛市黄岛区，请帮我找到 25 公里内合适的登山地点，
                并结合一些网络图片，制定一份详细的行程计划，
                并以 PDF 格式输出""";
        String answer = haxiManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}