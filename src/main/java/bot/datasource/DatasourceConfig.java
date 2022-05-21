package bot.datasource;

import bot.datasource.repositories.PostsRepository;
import bot.datasource.repositories.StatisticsRepository;
import bot.datasource.repositories.UsersRepository;
import bot.datasource.services.DBService;
import bot.datasource.services.JpaRepositoriesService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories
@EntityScan("/bot/entities")
public class DatasourceConfig {

    private static final String JDBC_URL = "jdbc:postgresql://ec2-52-18-116-67.eu-west-1.compute.amazonaws.com:5432/dbproh10v09til";
    private static final String JDBC_USERNAME = "tmrrpnytnnobjq";
    private static final String JDBC_PASSWORD = "e22795316214d7ceefde32831ce1e03c082ac87e57b9679abbdfce5264ea5199";
    private static final int JDBC_MAX_CONNECTION_POOL = 5;

    @Bean
    public DataSource dataSource() {
        HikariConfig dataSourceConfig = new HikariConfig();

        dataSourceConfig.setJdbcUrl(JDBC_URL);
        dataSourceConfig.setDriverClassName("org.postgresql.Driver");
        dataSourceConfig.setUsername(JDBC_USERNAME);
        dataSourceConfig.setPassword(JDBC_PASSWORD);
        dataSourceConfig.setMaximumPoolSize(JDBC_MAX_CONNECTION_POOL);

        return new HikariDataSource(dataSourceConfig);
    }

    @Bean(name = "service")
    public DBService service(UsersRepository usersRepository, PostsRepository postsRepository,
                             StatisticsRepository statisticsRepository) {
        return new JpaRepositoriesService(usersRepository, postsRepository, statisticsRepository);
    }
}
