package com.haxi.controller;

import com.haxi.agent.HaxiManus;
import com.haxi.app.TravelApp;
import com.haxi.tools.GaoDeIPTool;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private TravelApp travelApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private GaoDeIPTool gaoDeIPTool;

    /**
     * 同步调用 AI 自由行大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/travel_app/chat/sync")
    public String doChatWithTravelAppSync(String message, String chatId) {
        return travelApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 自由行大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/travel_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithTravelAppSSE(String message, String chatId) {
        return travelApp.doChatByStream(message, chatId);
    }

    /**
     * SSE 流式调用 AI 自由行大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/travel_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithTravelAppServerSentEvent(String message, String chatId) {
        return travelApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用 AI 自由行大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/travel_app/chat/sse_emitter")
    public SseEmitter doChatWithTravelAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        travelApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        HaxiManus haxiManus = new HaxiManus(allTools, dashscopeChatModel);
        return haxiManus.runStream(message);
    }

    /**
     * 获取用户当前位置信息（通过 IP 地址）
     *
     * @param request HTTP 请求
     * @param ip      可选的 IP 地址参数
     * @return 城市位置信息
     */
    @GetMapping("/location/ip")
    public ResponseEntity<String> getUserLocationByIP(
            HttpServletRequest request,
            @RequestParam(required = false) String ip) {
        
        // 如果没有传入 IP，尝试获取公网 IP
        if (ip == null || ip.isEmpty()) {
            ip = getPublicIP(request);
        }
        
        String result = gaoDeIPTool.getCityByIP(ip);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取公网 IP 地址（优先）或客户端真实 IP 地址
     * 优先通过第三方 API 获取公网 IP，失败时降级到本地获取
     */
    private String getPublicIP(HttpServletRequest request) {
        // 1. 首先尝试通过多个备用 API 获取公网 IP
        String[] apiUrls = {
            "http://ip-api.com/json/?lang=zh-CN"
        };
        
        for (String url : apiUrls) {
            try {
                String response = HttpUtil.get(url, 3000);
                String publicIp = extractIpFromResponse(response, url);
                if (publicIp != null && !publicIp.isEmpty()) {
                    System.out.println("✓ 成功获取公网 IP (" + url + "): " + publicIp);
                    return publicIp;
                }
            } catch (Exception e) {
                System.out.println("⚠ API 失败 [" + url + "]: " + e.getMessage());
                // 继续尝试下一个 API
            }
        }
        
        // 2. 降级方案：从 HTTP 请求头获取
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            // 如果是本地回环地址，尝试获取本机真实 IP
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ip = inetAddress.getHostAddress();
                    System.out.println("ℹ 使用本机 IP: " + ip);
                } catch (UnknownHostException e) {
                    System.out.println("⚠ 无法获取本机 IP: " + e.getMessage());
                }
            }
        }
        // 多个代理时，第一个 IP 是真实客户端 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 从不同 API 的响应中提取 IP 地址
     */
    private String extractIpFromResponse(String response, String url) {
        try {
            JSONObject json = new JSONObject(response);
            
            // ipify
            if (url.contains("ipify")) {
                return json.getStr("ip");
            }
            
            // 360
            if (url.contains("360.cn")) {
                JSONObject data = json.getJSONObject("data");
                return data != null ? data.getStr("ip") : null;
            }
            
            //太平洋网络
            if (url.contains("pconline")) {
                return json.getStr("ip");
            }
            
            // ip-api.com
            if (url.contains("ip-api.com")) {
                return json.getStr("query");
            }
        } catch (Exception e) {
            System.out.println("⚠ 解析 IP 响应失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 获取客户端真实 IP 地址（考虑代理情况）
     * @deprecated 已废弃，请使用 getPublicIP
     */
    @Deprecated
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            // 如果是本地回环地址，尝试获取本机真实 IP
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ip = inetAddress.getHostAddress();
                } catch (UnknownHostException e) {
                    // 保持原 IP
                }
            }
        }
        // 多个代理时，第一个 IP 是真实客户端 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}