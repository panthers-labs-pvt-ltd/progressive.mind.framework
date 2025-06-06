package com.progressive.minds.chimera;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.progressive.minds.chimera")
@MapperScan("com.progressive.minds.chimera.mapper")
public class ChimeraDataIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChimeraDataIngestionApplication.class, args);
    }
}
