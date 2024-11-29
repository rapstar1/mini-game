package com.example.demo.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private String secretKey = "yourSecretKey";  

    // 生成 JWT
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)  // 设置 JWT 的 subject（通常为用户名）
                .setIssuedAt(new Date())  // 设置生成时间
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // 过期时间（1小时）
                .signWith(SignatureAlgorithm.HS256, secretKey)  // 使用 HMAC 算法和密钥加密
                .compact();
    }

    // 提取用户名（subject）
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // 提取 JWT 中的所有 Claims
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证 JWT 是否过期
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // 校验 token 是否有效
    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }
}
