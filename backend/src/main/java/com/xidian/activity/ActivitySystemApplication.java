package com.xidian.activity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 高校学生活动全流程管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.xidian.activity.mapper")
public class ActivitySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivitySystemApplication.class, args);
    }
}
