package org.example.mail.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//测试用
@RestController
@RequestMapping("/api/discord")
public class DiscordController {

    @PostMapping("/send")
    public static void sendMessage(@RequestParam String webhookUrl, @RequestParam String content) throws IOException {
        URL url = new URL(webhookUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonPayload = "{\"content\":\"" + content + "\"}";        //通过这部分可以设置发送者账号的username和头像
        /*
        String jsonPayload = """
        {
            "username": "notification",
            "avatar_url": "https://example.com/avatar.jpg",
            "content": ""
        }
        """;
         */

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 204) {
            throw new RuntimeException("发送 Discord 消息失败，HTTP 状态码: " + responseCode);
        }
    }
}
