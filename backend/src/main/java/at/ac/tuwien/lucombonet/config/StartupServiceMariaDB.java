package at.ac.tuwien.lucombonet.config;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;

@Service
@Profile("mariadb")
public class StartupServiceMariaDB {

    @PostConstruct
    public static void setUpView() throws Exception {
        DriverManagerDataSource dataSource = getDataSource();
        Connection conn = dataSource.getConnection();
        ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/createView.sql"));
    }

    private static DriverManagerDataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:mariadb://localhost:3306/lucombonet");
        dataSource.setUsername("lucombonet");
        dataSource.setPassword("password");
        return dataSource;
    }
}
