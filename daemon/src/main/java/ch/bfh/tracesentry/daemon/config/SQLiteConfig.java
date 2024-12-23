package ch.bfh.tracesentry.daemon.config;

import ch.bfh.tracesentry.daemon.common.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class SQLiteConfig {

    @Value("${spring.datasource.url:}")
    private String url;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    public DataSource dataSource() {
        final String codeSrcPath = SQLiteConfig.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        final Path dbPath;
        if (codeSrcPath.contains(Constants.JAR_EXTENSION)) {
            // in jar mode
            final String tsDir = System.getenv(Constants.TS_DIR_ENV_VARIABLE);
            if (tsDir == null) {
                throw new IllegalStateException(Constants.TS_DIR_ENV_VARIABLE + " must be set!");
            }
            dbPath = Paths.get(tsDir);
        } else {
            // in dev mode
            dbPath = Paths.get(Constants.DAEMON_MODULE_NAME);
        }

        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url.isEmpty() ? getSQLiteUrl(dbPath) : url);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    private static String getSQLiteUrl(Path dbPath) {
        final String prefix = "jdbc:sqlite:";
        return prefix + dbPath.resolve(Constants.DB_NAME);
    }

}
