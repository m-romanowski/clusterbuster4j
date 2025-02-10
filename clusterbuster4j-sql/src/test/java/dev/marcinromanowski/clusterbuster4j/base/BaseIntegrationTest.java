package dev.marcinromanowski.clusterbuster4j.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BaseIntegrationTest {

    private static final PostgreSQL POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQL();
        POSTGRESQL_CONTAINER.startContainer();

        Runtime.getRuntime()
            .addShutdownHook(new Thread(POSTGRESQL_CONTAINER::close));
    }

    protected static String getJdbcUrl() {
        return POSTGRESQL_CONTAINER.getJdbcUrl();
    }

    protected static String getPgUsername() {
        return POSTGRESQL_CONTAINER.getUsername();
    }

    protected static String getPgPassword() {
        return POSTGRESQL_CONTAINER.getPassword();
    }

    protected Connection getJdbcConnection() throws SQLException {
        return DriverManager.getConnection(getJdbcUrl(), getPgUsername(), getPgPassword());
    }

}
