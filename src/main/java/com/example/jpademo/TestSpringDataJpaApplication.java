package com.example.jpademo;

import io.r2dbc.spi.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestSpringDataJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestSpringDataJpaApplication.class, args);
    }

    @Bean
    DSLContext jooqDSLContext(ConnectionFactory cfi) {
        return DSL.using(cfi).dsl();
    }
}
