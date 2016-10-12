package ee.iglu.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

@Slf4j
@RequiredArgsConstructor
public class ConstructorRowMapper<T> implements RowMapper<T> {

	private int rowCount = 0;

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		log.debug("mapping row nr {}", rowNum);
		rowCount++;
		return null;
	}

	public int getRowCount() {
		return rowCount;
	}
}
