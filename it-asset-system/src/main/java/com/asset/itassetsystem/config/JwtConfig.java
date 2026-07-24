package com.asset.itassetsystem.config;

import com.asset.itassetsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * JWT 配置：从 application.yml 读取密钥并初始化 JwtUtil
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:it-asset-system-default-secret-key-2026}")
    private String secret;

    @PostConstruct
    public void init() {
        JwtUtil.setSecret(secret);
    }
}
