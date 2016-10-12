package ee.iglu;

import javax.sql.DataSource;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@SpringBootConfiguration
public class Config {

	@Bean
	DataSource dataSource() {
		return new EmbeddedDatabaseBuilder()
				.generateUniqueName(true)
				.build();
	}

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}
