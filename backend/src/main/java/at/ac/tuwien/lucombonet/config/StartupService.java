package at.ac.tuwien.lucombonet.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;

@Service
public class StartupService {

    @PostConstruct
    public static void setUpView() throws Exception {
        DriverManagerDataSource dataSource = getDataSource();
        Connection conn = dataSource.getConnection();
        ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/createView.sql"));
    }

    private static DriverManagerDataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/lucombonet");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        return dataSource;
    }
}
