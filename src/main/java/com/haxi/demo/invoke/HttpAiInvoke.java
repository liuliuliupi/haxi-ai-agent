package com.haxi.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * Http方式调用AI
 */
public class HttpAiInvoke {
    public static void main(String[] args) {
        String apiKey = "DASHSCOPE_API_KEY"; // 替换为实际的 API Key
        String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

        // 构建消息数组
        JSONArray messages = JSONUtil.createArray();
        messages.add(new JSONObject()
                .set("role", "system")
                .set("content", "You are a helpful assistant."));
        messages.add(new JSONObject()
                .set("role", "user")
                .set("content", "你是谁？"));

        // 构建请求体
        JSONObject requestBody = new JSONObject()
                .set("model", "qwen-plus")
                .set("messages", messages);

        // 发送 POST 请求
        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute();

        // 获取响应
        int status = response.getStatus();
        String responseBody = response.body();

        System.out.println("状态码：" + status);
        System.out.println("响应内容：" + responseBody);

        // 如果需要解析响应
        JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
        System.out.println("解析后的响应：" + jsonResponse);
    }
}
