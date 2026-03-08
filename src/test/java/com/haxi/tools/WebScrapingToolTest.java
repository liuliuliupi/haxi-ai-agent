package com.haxi.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://javaguide.cn/home.html";
        String result = webScrapingTool.scrapeWebPage(url);
        Assertions.assertNotNull(result);
    }
}