package com.yc.admin.auth.service;

import com.yc.admin.auth.config.JwtProperties;
import com.yc.admin.auth.dto.AuthLoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JWT 令牌服务
 * 负责 JWT 令牌的生成、验证、刷新和删除
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final CacheService cacheService;
    private final JwtProperties jwtProperties;
    
    /**
     * Redis 中用户信息的键前缀
     */
    private static final String LOGIN_USER_KEY = "login_user:";
    private static final String REFRESH_TOKEN_KEY = "refresh_token:";
    
    /**
     * JWT 中用户标识的键名
     */
    private static final String LOGIN_USER_UUID = "login_user_uuid";

    /**
     * 创建令牌
     * 
     * @param authLoginUser 登录用户信息
     * @return JWT 令牌
     */
    public String createToken(AuthLoginUser authLoginUser) {
        String uuid = UUID.randomUUID().toString();
        authLoginUser.setToken(uuid);
        
        // 将用户信息存储到 Redis
        refreshToken(authLoginUser);
        
        // 生成 JWT 令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(LOGIN_USER_UUID, uuid);
        
        return generateToken(claims);
    }

    /**
     * 获取登录用户信息
     * 
     * @param token JWT 令牌
     * @return 登录用户信息
     */
    public AuthLoginUser getLoginUser(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        
        try {
            Claims claims = parseToken(token);
            String uuid = (String) claims.get(LOGIN_USER_UUID);
            
            if (StringUtils.hasText(uuid)) {
                String userKey = getTokenKey(uuid);
                return (AuthLoginUser) cacheService.get(userKey);
            }
        } catch (Exception e) {
            log.warn("获取登录用户信息失败: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * 验证令牌
     * 
     * @param authLoginUser 登录用户信息
     * @return 是否有效
     */
    public boolean verifyToken(AuthLoginUser authLoginUser) {
        if (authLoginUser == null || !StringUtils.hasText(authLoginUser.getToken())) {
            return false;
        }
        
        // 检查令牌是否过期
        long expireTime = authLoginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        
        if (expireTime - currentTime <= Duration.ofMinutes(20).toMillis()) {
            // 令牌即将过期，刷新令牌
            refreshToken(authLoginUser);
        }
        
        return true;
    }

    /**
     * 刷新令牌有效期
     * @param authLoginUser 登录用户信息
     */
    public void refreshToken(AuthLoginUser authLoginUser) {
        authLoginUser.setLoginTime(System.currentTimeMillis());
        authLoginUser.setExpireTime(authLoginUser.getLoginTime() + jwtProperties.getExpiration());
        
        // 更新缓存中的用户信息
        String userKey = getTokenKey(authLoginUser.getToken());
        cacheService.set(userKey, authLoginUser, jwtProperties.getExpiration(), TimeUnit.MILLISECONDS);
    }

    /**
     * 删除令牌
     * 
     * @param token JWT 令牌
     */
    public void deleteToken(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        
        try {
            Claims claims = parseToken(token);
            String uuid = (String) claims.get(LOGIN_USER_UUID);
            
            if (StringUtils.hasText(uuid)) {
                String userKey = getTokenKey(uuid);
                cacheService.delete(userKey);
                log.info("令牌已删除: {}", uuid);
            }
        } catch (Exception e) {
            log.warn("删除令牌失败: {}", e.getMessage());
        }
    }

    /**
     * 生成 JWT 令牌
     * 
     * @param claims 声明
     * @return JWT 令牌
     */
    private String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 解析 JWT 令牌
     * 
     * @param token JWT 令牌
     * @return 声明
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取 Redis 中用户信息的键
     * 
     * @param uuid 用户唯一标识
     * @return Redis 键
     */
    private String getTokenKey(String uuid) {
        return LOGIN_USER_KEY + uuid;
    }

    /**
     * 获取令牌过期时间（秒）
     * @return 过期时间
     */
    public long getTokenExpiration() {
        return jwtProperties.getExpiration() / 1000;
    }
}