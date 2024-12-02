package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.Response;
import com.example.demo.models.InitUserRequest;
import com.example.demo.models.RollRequest;
import com.example.demo.models.SetWalletAddressRequest;
import com.example.demo.models.User;
import com.example.demo.services.UserService;
import com.example.demo.services.JwtUtil;  // 导入 JwtUtil 类

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ConcurrentHashMap;

import javax.validation.Valid;

import java.util.Map;
import java.util.Optional;


@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil 类，用于生成 JWT

    // Telegram Bot Token, 用于验证用户信息, 从环境变量中获取
    @Value("${bot.token}")
    private String BOT_TOKEN;

    /**
     * 初始化用户，验证用户信息并返回 JWT
     */
    @PostMapping("/init")
    public ResponseEntity<?> initUser(@Valid @RequestBody InitUserRequest initUserRequest) {
        String initData = initUserRequest.getInitdata();

        // 验证 initData 的完整性
        boolean isValid = userService.validateInitData(initData, BOT_TOKEN);

        if (isValid) {
            // 解析 initData 并提取 WebAppUser.ID
            String telegramID = userService.extractUserId(initData);

            // 根据 Telegram ID 获取用户信息, 如果用户不存在则创建新用户
            Optional<User> userOptional = userService.getUser(telegramID);
            if (userOptional.isEmpty()) {
                return createResponse(HttpStatus.NOT_FOUND, Map.of("message","Account not found."));
            }
            User user = userOptional.get();

            // 生成 JWT
            String token = jwtUtil.generateToken(user.getUsername());  // 使用 JWT 生成工具生成 token

            return createResponse(HttpStatus.OK, Map.of( 
                "message", "User verified successfully", 
                "data", Map.of(
                    "points", user.getPoints(),
                    "energy", user.getEnergy(),
                    "walletAddress", user.getWalletAddress(),
                    "jwt", token  // 返回生成的 JWT
                )
            ));

        } else {
            return createResponse(HttpStatus.BAD_REQUEST, Map.of("message","Invalid data"));
        }
    }

    /**
     * 掷骰子接口
     */
    @PostMapping("/roll")
    public ResponseEntity<?> roll(@Valid @RequestBody RollRequest rollRequest) {
        String telegramId = rollRequest.getTelegramId();
        Optional<User> user = userService.getUser(telegramId);
        if (user.isEmpty()) {
            return createResponse(HttpStatus.NOT_FOUND, Map.of("message","Account not found."));
        }
        String rolledNumber = userService.rollDice(user.get().getTelegramId());
        return createResponse(HttpStatus.OK, Map.of(
        "message", "Dice rolled successfully", 
        "data", Map.of(
            "rolledNumber", rolledNumber,
            "energy", user.getEnergy(),
            "points", user.getPoints()
            )
        ));
    }

    /**
     * 设置用户钱包地址接口
     */
    @PostMapping("/setWalletAddress")
    public ResponseEntity<?> setWalletAddress(@Valid @RequestBody SetWalletAddressRequest setWalletAddressRequest) {
        String walletAddress = setWalletAddressRequest.getWalletAddress();
        String telegramId = setWalletAddressRequest.getTelegramId();
        Optional<User> user = userService.getUser(telegramId);
        if (user.isEmpty()) {
            return createResponse(HttpStatus.NOT_FOUND, Map.of("message", "Account not found."));
        }
        userService.setWalletAddress(user.get(), walletAddress);
        return createResponse(HttpStatus.OK, Map.of("message", "Wallet address set successfully"));
    }

    // 定时任务，每 30 秒恢复用户能量值
    @Scheduled(fixedRate = 30000)
    public void recoverEnergy() {
        userService.recoverEnergy();
    }

    /**
     * 创建统一的响应对象
     */
    private ResponseEntity<Response> createResponse(HttpStatus status, Map<String, Object> message) {
        return ResponseEntity.status(status).body(new Response(status.value(), message));
    }

}
