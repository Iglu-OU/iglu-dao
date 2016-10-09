package ee.iglu;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@SpringBootConfiguration
public class Config {

	@Bean
	JdbcTemplate jdbcTemplate(){
		EmbeddedDatabase dataSource = new EmbeddedDatabaseBuilder()
				.generateUniqueName(true)
				.build();

		return new JdbcTemplate(dataSource);
	}
}
