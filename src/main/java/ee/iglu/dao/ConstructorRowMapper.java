package ee.iglu.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

@RequiredArgsConstructor
public class ConstructorRowMapper<T> implements RowMapper<T> {

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		return null;
	}

}
