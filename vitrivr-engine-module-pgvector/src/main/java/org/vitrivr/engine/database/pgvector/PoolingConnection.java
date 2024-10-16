package org.vitrivr.engine.database.pgvector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class PoolingConnection {


    private static HikariDataSource instance = null;

    public static Connection getConnection() throws SQLException {
        if (instance == null) {
            return getConnection(new Properties());
        }
        return instance.getConnection();
    }

    public static Connection getConnection(Properties props) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://10.34.64.130/postgres");
        config.setUsername("postgres");
        config.setPassword("admin");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        instance = new HikariDataSource(config);
        return instance.getConnection();
    }
}
