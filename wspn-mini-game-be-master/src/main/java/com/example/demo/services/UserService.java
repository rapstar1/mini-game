package com.example.demo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.example.demo.models.User;
import com.example.demo.repos.UserRepository;

import java.util.Optional;
import java.util.Random;

import javax.validation.Valid;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private final Random random = new Random();

    // 外部钱包 API 的 URL 和密钥
    @Value("${wallet.api.url}")
    private String walletApiUrl;  // 钱包 API 的基础 URL

    @Value("${wallet.api.key}")
    private String apiKey;  // 钱包 API 的密钥

    // 验证来自 Telegram initData 的完整性
    public boolean validateInitData(String initData, String botToken) {
        try {
            Map<String, String> params = parseQueryString(initData);
            String dataCheckString = createDataCheckString(params);
            String secretKey = hmacSha256(botToken, "WebAppData");
            String hash = params.get("hash");
            String calculatedHash = hmacSha256(dataCheckString, secretKey);
            return calculatedHash.equals(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 解析查询字符串
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new TreeMap<>();
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            params.put(keyValue[0], keyValue[1]);
        }
        return params;
    }

    // 创建数据校验字符串
    private String createDataCheckString(Map<String, String> params) {
        StringBuilder dataCheckString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("hash")) {
                dataCheckString.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
        }
        return dataCheckString.toString().trim();
    }

    // 生成 HMAC SHA-256 校验
    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hashBytes = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    // 从 initData 中提取 TelegramId
    public String extractUserId(String initData) {
        JSONObject jsonObject = new JSONObject(initData);
        return jsonObject.getJSONObject("user").getString("id");
    }

    // 根据 TelegramId 获取用户信息，如果用户不存在则创建新用户，返回 Optional<User>
    public Optional<User> getUser(String telegramId) {
        User user = userRepository.findByTelegramId(telegramId);
        // 如果用户不存在，则创建新用户
        if (user == null) {
            user = new User();
            user.setTelegramId(telegramId);
            user.setEnergy(20); // 初始化能量值
            user.setPoints(0); // 初始化积分
            userRepository.save(user); // 保存新用户
        }
        return Optional.ofNullable(user);
    }

    // 掷骰子，更新用户积分和能量值
    public String rollDice(String telegramId) {
        User user = userRepository.findByTelegramId(telegramId);
        if (user == null) {
            return "User does not exist";
        }
        if (user.getEnergy() <= 0) {
            return "Not enough energy to play";
        }
        // 每次玩游戏消耗 1 点能量
        user.setEnergy(user.getEnergy() - 1);
        double rolledNumber = generateRandomNumber();
        updatePoints(user, rolledNumber);
        userRepository.save(user); // 保存用户对象
        return String.valueOf(rolledNumber);
    }

    // 生成随机数，模拟骰子点数范围为 1 到 20 (D20)
    private double generateRandomNumber() {
        return random.nextInt(20) + 1; // 生成 1 到 20 之间的随机数
    }

    // 根据骰子点数更新用户积分
    private void updatePoints(User user, double rolledNumber) {
        // 如果点数为 20，则增加 999 积分， 如果点数为 1，则减少 999 积分， 否则增加点数
        if (rolledNumber == 20) {
            user.setPoints(user.getPoints() + 999);
        } else if (rolledNumber == 1) {
            user.setPoints(Math.max(user.getPoints() - 999, 0)); // 确保积分不会小于0
        } else {
            user.setPoints(user.getPoints() + rolledNumber);
        }
    }

    // 设置钱包地址
    public void setWalletAddress(User user, String walletAddress) {
        user.setWalletAddress(walletAddress);
        userRepository.save(user); // 保存用户对象
    }

    // 恢复用户能量值
    public void recoverEnergy() {
        userRepository.incrementEnergyForAllUsersWithLessThanMaxEnergy(20, 1);
    }

    // 验证钱包地址
    public boolean validateWalletAddress(String inputAddress) {
        // 使用 RestTemplate 访问外部 API 验证钱包地址
        String url = String.format("%s/v1/users/find?chainId=1&address=%s", walletApiUrl, inputAddress);

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("accept", "application/json");
            headers.set("x-api-key", apiKey);  // 设置 API 密钥

            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 使用 RestTemplate 发送 GET 请求到钱包 API
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // 假设外部 API 返回 "VALID" 表示地址有效
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().contains("valid")) {
                return true;  // 地址有效
            }

            return false;  // 地址无效
        } catch (Exception e) {
            // 如果发生异常（比如网络问题），返回 false
            return false;
        }
    }
}

     */
    private ResponseEntity<Response> createResponse(HttpStatus status, Map<String, Object> message) {
        return ResponseEntity.status(status).body(new Response(status.value(), message));
    }
}
