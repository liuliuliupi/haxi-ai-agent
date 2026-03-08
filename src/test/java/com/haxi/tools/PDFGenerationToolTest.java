package com.haxi.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "自由行智能体.pdf";
        String content = "毕设项目 https://github/liuliuliupi";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}