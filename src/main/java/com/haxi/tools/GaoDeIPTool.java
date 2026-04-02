package com.haxi.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 高德 IP 地址定位工具
 */
@Component
public class GaoDeIPTool {

    private static final String IP_API_URL = "https://restapi.amap.com/v3/ip";

    private final String apiKey;

    public GaoDeIPTool(@Value("${gaode.api.key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "根据 IP 地址查询所在城市信息，用于旅行规划时确定用户当前位置或出发地")
    public String getCityByIP(
            @ToolParam(description = "要查询的 IP 地址，如果不传则自动获取请求者 IP") String ip) {
        Map<String, Object> paramMap = new HashMap<>();
        
        if (ip != null && !ip.isEmpty() && !"null".equals(ip)) {
            paramMap.put("ip", ip);
        }
        
        paramMap.put("key", apiKey);
        paramMap.put("output", "json");
        
        try {
            String response = HttpUtil.get(IP_API_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            
            String status = jsonObject.getStr("status");
            String info = jsonObject.getStr("info");
            
            if ("1".equals(status) && "OK".equals(info)) {
                String province = jsonObject.getStr("province", "");
                String city = jsonObject.getStr("city", "");
                String adcode = jsonObject.getStr("adcode", "");
                String rectangle = jsonObject.getStr("rectangle", "");
                
                JSONObject result = new JSONObject();
                result.put("province", province);
                result.put("city", city);
                result.put("adcode", adcode);
                result.put("rectangle", rectangle);
                result.put("location", city.isEmpty() ? province : city);
                
                return result.toString();
            } else {
                return "{\"error\": true, \"message\": \"" + info + "\"}";
            }
        } catch (Exception e) {
            return "{\"error\": true, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
    
    @Tool(description = "获取当前用户所在城市信息（自动识别 IP）")
    public String getCurrentUserLocation() {
        return getCityByIP(null);
    }
}
