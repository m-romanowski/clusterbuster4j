package dev.marcinromanowski.clusterbuster4j.base;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.Closeable;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

class PostgreSQL implements Closeable {

    private static final String DEFAULT_USERNAME = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";
    private static final String DEFAULT_DB_NAME = "test";

    private PostgreSQLContainer<?> container;

    @SuppressWarnings("resource")
    void startContainer() {
        if (isNull(container)) {
            final DockerImageName image = DockerImageName.parse("postgis/postgis:15-3.5-alpine")
                .asCompatibleSubstituteFor("postgres");
            container = new PostgreSQLContainer<>(image)
                .withUsername(DEFAULT_USERNAME)
                .withPassword(DEFAULT_PASSWORD)
                .withDatabaseName(DEFAULT_DB_NAME)
                .withInitScript("sql/init.sql");
            container.start();
        }
    }

    String getJdbcUrl() {
        if (nonNull(container)) {
            return container.getJdbcUrl();
        }

        throw new IllegalStateException("PostgreSQL container not started");
    }

    String getUsername() {
        return DEFAULT_USERNAME;
    }

    String getPassword() {
        return DEFAULT_PASSWORD;
    }

    @Override
    public void close() {
        if (nonNull(container)) {
            container.close();
        }
    }

}
