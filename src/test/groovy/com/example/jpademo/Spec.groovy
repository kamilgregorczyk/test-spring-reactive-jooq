package com.example.jpademo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresInitalizer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Spec extends Specification {

    @Autowired
    WebTestClient client

    private static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:11.4")
        .withDatabaseName("test-db")

    static {
        postgreDBContainer.start()
    }

    public static class PostgresInitalizer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.r2dbc.url=" + postgreDBContainer.getJdbcUrl().replace("jdbc", "r2dbc"),
                "spring.flyway.url=" + postgreDBContainer.getJdbcUrl(),
                "spring.r2dbc.username=" + postgreDBContainer.getUsername(),
                "spring.r2dbc.password=" + postgreDBContainer.getPassword()
            );
        }
    }

}
