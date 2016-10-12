package ee.iglu.dao;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Ignore;
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
	public void row_mapper_is_executed() {
		ConstructorRowMapper<SimpleRow> rowMapper = new ConstructorRowMapper<>();

		jdbcTemplate.query("SELECT * FROM simple", rowMapper);

		assertThat(rowMapper.getRowCount(), greaterThan(0));
	}

	@Test
	@Ignore
	public void failing_test() {
		fail("EPIC FAIL!");
	}
}
