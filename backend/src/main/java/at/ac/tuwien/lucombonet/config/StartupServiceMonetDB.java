package at.ac.tuwien.lucombonet.config;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;

@Service
@Profile("monet")
public class StartupServiceMonetDB {

    @PostConstruct
    public static void setUpView() throws Exception {
        DriverManagerDataSource dataSource = getDataSource();
        Connection conn = dataSource.getConnection();
        ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/createView.sql"));
    }

    private static DriverManagerDataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:monetdb://localhost:50000/demo");
        dataSource.setDriverClassName("nl.cwi.monetdb.jdbc.MonetDriver");
        dataSource.setUsername("monetdb");
        dataSource.setPassword("monetdb");
        return dataSource;
    }
}
