package at.ac.tuwien.lucombonet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Values {

    @Value("jdbc:mysql://localhost:3306/lucombonet?useUnicode=true&connectionCollation=utf8mb4_bin&characterSetResults=utf8")
    private String dbUrl;

    @Value("lucombonet")
    private String dbUser;

    @Value("password")
    private String dbPassword;

    @Value("com.mysql.cj.jdbc.Driver")
    private String dbDriver;

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbDriver() {
        return dbDriver;
    }

}