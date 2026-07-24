package com.asset.itassetsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IT 固定资产管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.asset.itassetsystem.mapper")
public class ItAssetSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItAssetSystemApplication.class, args);
    }
}
