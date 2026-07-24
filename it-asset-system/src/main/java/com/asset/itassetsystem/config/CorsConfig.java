package com.asset.itassetsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有接口跨域
        registry.addMapping("/**")
                // 允许所有域名访问（生产环境需指定具体域名）
                .allowedOriginPatterns("*")
                // 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带 Token（禁用 credentials 以兼容通配符域名）
                .allowCredentials(false)
                // 预检请求有效期（秒）
                .maxAge(3600);
    }
}
