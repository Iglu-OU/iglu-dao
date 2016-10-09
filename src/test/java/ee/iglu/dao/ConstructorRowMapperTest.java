package ee.iglu.dao;

import static org.junit.Assert.fail;

import org.junit.Test;

public class ConstructorRowMapperTest {

	@Test
	public void dummy_test() {
		new ConstructorRowMapper<>();
	}

	@Test
	public void failing_test() {
		fail("EPIC FAIL!");
	}
}
