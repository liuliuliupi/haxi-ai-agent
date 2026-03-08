package com.haxi.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDownloadToolTest {

    @Test
    public void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://www.w3schools.com/w3css/img_lights.jpg";
        String fileName = "picture.jpg";
        String result = tool.downloadResource(url, fileName);
        System.out.println("下载结果：" + result);
        assertNotNull(result);
    }
}