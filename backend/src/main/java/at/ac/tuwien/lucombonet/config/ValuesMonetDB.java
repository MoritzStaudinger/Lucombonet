package at.ac.tuwien.lucombonet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("monet")
@Configuration
public class ValuesMonetDB implements Values{

    @Value("jdbc:monetdb://localhost:50000/demo")
    private String dbUrl;

    @Value("monetdb")
    private String dbUser;

    @Value("monetdb")
    private String dbPassword;

    @Value("nl.cwi.monetdb.jdbc.MonetDriver")
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