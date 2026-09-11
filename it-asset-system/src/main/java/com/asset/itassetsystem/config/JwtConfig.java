package com.asset.itassetsystem.config;

import com.asset.itassetsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * JWT 配置：从 application.yml 读取密钥并初始化 JwtUtil。
 *
 * 启动期强校验（第二道 fail-fast）：密钥非空、长度 ≥ 32、且不在已知弱默认值黑名单中，
 * 任一不满足即抛异常终止启动。第三道防线位于 {@link JwtUtil#setSecret(String)}。
 */
@Configuration
public class JwtConfig {

    /** 已知弱默认密钥黑名单（历史值全列，任一命中即拒绝启动）。 */
    private static final Set<String> WEAK_SECRETS = Set.of(
            "it-asset-system-default-secret-key",
            "it-asset-system-default-secret-key-2026");

    /** 密钥（无默认值：必须由环境变量 JWT_SECRET 注入）。 */
    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET 未配置，拒绝启动");
        }
        if (secret.trim().length() < 32) {
            throw new IllegalStateException("JWT_SECRET 过短（<32），拒绝启动");
        }
        if (WEAK_SECRETS.contains(secret.trim())) {
            throw new IllegalStateException("检测到已知弱默认 JWT 密钥，拒绝启动");
        }
        // 交由 JwtUtil 再做一次防御性校验
        JwtUtil.setSecret(secret);
    }
}
