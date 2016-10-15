package ee.iglu.dao;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@Sql
@RunWith(SpringRunner.class)
@SpringBootTest
public class ConstructorRowMapperTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void row_mapper_returns_populated_objects() {
		ConstructorRowMapper<SimpleRow> rowMapper = new ConstructorRowMapper<>(SimpleRow.class);

		List<SimpleRow> rows = jdbcTemplate.query("SELECT * FROM simple", rowMapper);

		assertThat(rows, hasSize(greaterThan(0)));
		SimpleRow row = rows.get(0);
		assertThat(row, notNullValue());
		assertThat(row.getId(), equalTo(10L));
		assertThat(row.getText(), equalTo("lorem ipsum..."));
	}

}
