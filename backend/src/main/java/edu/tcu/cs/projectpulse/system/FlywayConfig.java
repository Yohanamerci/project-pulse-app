package edu.tcu.cs.projectpulse.system;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Manual Flyway configuration — required because Spring Boot 4.0.x does not
 * include Flyway auto-configuration in its core autoconfigure module.
 *
 * baseline-on-migrate: if a schema already exists with no flyway_schema_history
 * (e.g., a developer's pre-existing database), Flyway baselines at V1 and only
 * applies migrations newer than V1 (i.e., V2+). V2 uses INSERT IGNORE so
 * running it on a pre-populated database is safe.
 */
@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .validateOnMigrate(false)
                .load();
    }
}
