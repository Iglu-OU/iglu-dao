package ee.iglu.dao;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConstructorRowMapperTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void dummy_test() {
		ConstructorRowMapper<Object> rowMapper = new ConstructorRowMapper<>();
	}

	@Test
	@Ignore
	public void failing_test() {
		fail("EPIC FAIL!");
	}
}
