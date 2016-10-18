package ee.iglu.dao;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.DefaultConversionService;
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

	@Test
	public void check_fully_populated() {
		ConstructorRowMapper<SimpleRow> rowMapper = new ConstructorRowMapper<>(SimpleRow.class);

		try {
			jdbcTemplate.query("SELECT id, text AS columnname FROM simple", rowMapper);
		} catch (Exception e) {
			assertThat(e.getMessage(), containsString("missing properties: [text]"));
			assertThat(e.getMessage(), containsString("excess columns: [columnname]"));
			return;
		}

		fail("Expected exception because of checkFullyPopulated failure");
	}

	@Test
	public void test_complex_row() {

		ConstructorRowMapper<ComplexRow> rowMapper =
				new ConstructorRowMapper<>(ComplexRow.class, new DefaultConversionService());
		List<ComplexRow> result = jdbcTemplate.query("SELECT * FROM complex", rowMapper);
		assertThat(result, hasSize(1));

		ComplexRow row = result.get(0);
		assertThat(row.getId(), equalTo(new CustomWrapper(7L)));
		assertThat(row.getText(), equalTo("lorem ipsum..."));
		assertThat(row.isMultiPartName(), equalTo(true));
		assertThat(row.getUuid(), equalTo(UUID.fromString("b879edb0-a15e-4712-a778-b1845037495e")));
		assertThat(row.getInstant(), equalTo(Instant.parse("2016-10-18T10:06:49.582Z")));
	}
}
