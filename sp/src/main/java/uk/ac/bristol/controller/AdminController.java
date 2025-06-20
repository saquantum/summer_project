package uk.ac.bristol.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.UserAsAssetHolder;
import uk.ac.bristol.service.SqlService;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SqlService sqlService;

    /**
     * Instant Messaging: Client receives a message only when it's online.
     * To improve: Use Message Queue (Kafka / RabbitMQ / RocketMQ) with database (Redis)
     * <br>
     * <a href="https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html">Spring Stomp</a>
     */
    @PostMapping("/notify")
    public ResponseResult sendNotification(@RequestBody Map<String, String> body) {
        String m = body.get("message");
        messagingTemplate.convertAndSend("/topic/notify", m);
        return new ResponseResult(Code.SUCCESS, null, "Notification sent");
    }

    @GetMapping("/as/{id}")
    public ResponseResult asUser(HttpServletResponse response, HttpServletRequest request, @PathVariable Long id) throws IOException {
        Claims claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request, response));
        claims.put("asUserId", id);
        JwtUtil.bindJWTAsCookie(response, JwtUtil.generateJWT(claims));
        return new ResponseResult(Code.SUCCESS, null, "You are in proxy mode as user id " + id);
    }

    @GetMapping("/as/{id}/inAsset/{assetId}")
    public ResponseResult asUserInAsset(HttpServletResponse response, HttpServletRequest request, @PathVariable Long id, @PathVariable Long assetId) throws IOException {
        Claims claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request, response));
        claims.put("asUserId", id);
        claims.put("asUserInAssetId", assetId);
        JwtUtil.bindJWTAsCookie(response, JwtUtil.generateJWT(claims));
        return new ResponseResult(Code.SUCCESS, null, "You are in proxy mode as user id " + id + " in asset " + assetId);
    }

    @GetMapping("/as/clear")
    public ResponseResult clearProxy(HttpServletResponse response, HttpServletRequest request) throws IOException {
        Claims claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request, response));
        claims.remove("asUserId");
        claims.remove("asUserInAssetId");
        JwtUtil.bindJWTAsCookie(response, JwtUtil.generateJWT(claims));
        return new ResponseResult(Code.SUCCESS, null, "Proxy mode cleared.");
    }

    @PostMapping("/user")
    public ResponseResult insertAssetHolders(@RequestBody List<UserAsAssetHolder> list){
        for (UserAsAssetHolder u : list) {
            sqlService.insertUser(u);
        }
        return new ResponseResult(Code.INSERT_OK, null);
    }
}
