package com.muebleria.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {
    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String url = env.getProperty("SPRING_DATASOURCE_URL");
        String user = env.getProperty("SPRING_DATASOURCE_USERNAME");
        String pass = env.getProperty("SPRING_DATASOURCE_PASSWORD");
        if (url != null && !url.isBlank()) {
            String normalized = url.startsWith("jdbc:") ? url.substring(5) : url;
            if (normalized.startsWith("postgresql://")) {
                URI uri = URI.create(normalized);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] parts = userInfo.split(":", 2);
                    if (user == null || user.isBlank()) user = parts[0];
                    if (pass == null || pass.isBlank()) pass = parts.length > 1 ? parts[1] : pass;
                }
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String db = uri.getPath();
                if (db != null && db.startsWith("/")) db = db.substring(1);
                url = "jdbc:postgresql://" + host + ":" + port + "/" + db + "?sslmode=require";
            }
        } else {
            String dbUrl = env.getProperty("DATABASE_URL");
            if (dbUrl != null && !dbUrl.isBlank()) {
                URI uri = URI.create(dbUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] parts = userInfo.split(":", 2);
                    if (user == null || user.isBlank()) user = parts[0];
                    if (pass == null || pass.isBlank()) pass = parts.length > 1 ? parts[1] : pass;
                }
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String db = uri.getPath();
                if (db != null && db.startsWith("/")) db = db.substring(1);
                url = "jdbc:postgresql://" + host + ":" + port + "/" + db + "?sslmode=require";
            }
        }
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(url != null ? url : "jdbc:postgresql://localhost:5432/muebleria_87vm?sslmode=require");
        ds.setUsername(user != null ? user : "postgres");
        ds.setPassword(pass != null ? pass : "");
        return ds;
    }
}
