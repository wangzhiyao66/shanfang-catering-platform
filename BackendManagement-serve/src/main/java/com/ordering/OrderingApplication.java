package com.ordering;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 点餐小程序后端启动类。
 * 监听 3000 端口；/api/client 与 /api/admin 共用同一 Service / DAO 与同一数据库。
 */
@SpringBootApplication
@MapperScan("com.ordering.modules.**.mapper")
public class OrderingApplication {

    public static void main(String[] args) {

        SpringApplication.run(OrderingApplication.class, args);

        System.out.println(" ====== 服务启动成功了 ====== started successfully on port 3000.");
    }
}
