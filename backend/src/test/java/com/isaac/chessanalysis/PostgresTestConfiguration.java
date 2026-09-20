package com.isaac.chessanalysis;

import java.io.IOException;
import javax.sql.DataSource;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestConfiguration {
    @Bean(destroyMethod = "close")
    EmbeddedPostgres postgres() throws IOException {
        return EmbeddedPostgres.builder().setPort(0).start();
    }

    @Bean
    DataSource dataSource(EmbeddedPostgres postgres) {
        return postgres.getPostgresDatabase();
    }
}
