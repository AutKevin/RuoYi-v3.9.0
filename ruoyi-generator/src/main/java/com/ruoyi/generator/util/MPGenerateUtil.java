package com.ruoyi.generator.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;

public class MPGenerateUtil {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://192.168.10.195:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
                        "root",
                        "root")
                .globalConfig(builder -> builder
                        .author("Baomidou")
                        .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java")
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent("com.baomidou.mybatisplus")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .xml("mapper.xml")
                )
                .strategyConfig(builder -> builder
                        .addInclude("sys_config") // 设置需要生成的表名
                        //.addTablePrefix("t_", "c_") // 设置过滤表前缀
                        .entityBuilder().enableTableFieldAnnotation() // 给字段加 @TableField 注解
                        .controllerBuilder().enableRestStyle() // 生成 REST 风格 Controller
                        .entityBuilder()
                        .enableLombok()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
