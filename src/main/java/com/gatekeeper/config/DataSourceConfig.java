package com.gatekeeper.config;

import com.gatekeeper.components.EnvironmentUtils;
import com.gatekeeper.components.SpringShutdownUtil;
import com.gatekeeper.exceptions.EnvironmentValidationException;
import com.gatekeeper.exceptions.UnsupportedbTypeException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author null
 */
@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);
    private final EnvironmentUtils environmentUtils;
    private final SpringShutdownUtil shutdownUtil;

    public DataSourceConfig(EnvironmentUtils environmentUtils, SpringShutdownUtil shutdownUtil) {
        this.environmentUtils = environmentUtils;
        this.shutdownUtil = shutdownUtil;
    }

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        String dbType = "", url = "";
        try {
            dbType = environmentUtils.validateEnvVar(Constants.ENV_DB_TYPE);
            url = buildDataSourceUrl(dbType);
            
        } catch (EnvironmentValidationException ex) {
            logger.error(ex.getMessage());
            shutdownUtil.shutDownSpringApp();
        }
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(getDriverClassName(dbType));
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        return new HikariDataSource(hikariConfig);
    }

    private String buildDataSourceUrl(String dbType) throws EnvironmentValidationException, 
            UnsupportedbTypeException {
        String host = environmentUtils.validateEnvVar(Constants.ENV_DB_HOST);
        String port = environmentUtils.validateEnvVar(Constants.ENV_DB_PORT);
        String name = environmentUtils.validateEnvVar(Constants.ENV_DB_NAME);

        switch (dbType.toLowerCase()) {
            case "mariadb" -> {
                return "jdbc:mariadb://" + host + ":" + port + "/" + name;
            }
            case "mysql" -> {
                return "jdbc:mysql://" + host + ":" + port + "/" + name;
            }
            case "postgres" -> {
                return "jdbc:postgresql://" + host + ":" + port + "/" + name;
            }
            case "db2" -> {
                return "jdbc:db2://" + host + ":" + port + "/" + name;
            }
            case "mssql" -> {
                return "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + name;
            }
            case "sqlite" -> {
                return "jdbc:sqlite:" + name; // SQLite does not use host and port
            }
            default ->
                throw new UnsupportedbTypeException(dbType);
        }
    }

    private String getDriverClassName(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mariadb" -> {
                return "org.mariadb.jdbc.Driver";
            }
            case "mysql" -> {
                return "com.mysql.cj.jdbc.Driver";
            }
            case "postgres" -> {
                return "org.postgresql.Driver";
            }
            case "db2" -> {
                return "com.ibm.db2.jcc.DB2Driver";
            }
            case "mssql" -> {
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            }
            case "sqlite" -> {
                return "org.sqlite.JDBC";
            }
            default ->
                throw new UnsupportedbTypeException(dbType);
        }
    }
}
