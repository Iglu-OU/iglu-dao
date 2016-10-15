package ee.iglu.dao;

import static com.google.common.base.Preconditions.checkArgument;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

import com.google.common.base.Throwables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

@Slf4j
@RequiredArgsConstructor
public class ConstructorRowMapper<T> implements RowMapper<T> {
	private final Class<T> rowClass;

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		log.debug("mapping row nr {}", rowNum);

		Constructor<T> constructor = findConstructor(rowClass);
		Class<?>[] parameterTypes = constructor.getParameterTypes();

		String[] parameterNames = constructor.getAnnotation(ConstructorProperties.class).value();
		int[] columnIndexes = createParameterToColumnMap(rs.getMetaData(), parameterNames);
		Object[] arguments = getArguments(rs, columnIndexes, parameterTypes);

		try {
			return constructor.newInstance(arguments);
		} catch (InstantiationException e) {
			throw Throwables.propagate(e);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		} catch (InvocationTargetException e) {
			throw Throwables.propagate(e);
		}
	}

	private int[] createParameterToColumnMap(ResultSetMetaData metaData, String[] parameterNames) throws SQLException {
		int columnCount = metaData.getColumnCount();
		int parameterCount = parameterNames.length;

		int[] columnIndexes = new int[parameterCount];
		for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
			String parameterName = getParameterNameForColumn(metaData, columnIndex);
			int parameterIndex = findParameterIndex(parameterNames, parameterName);
			if (parameterIndex != -1) {
				columnIndexes[parameterIndex] = columnIndex;
			}
		}

		return columnIndexes;
	}

	private String getParameterNameForColumn(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		String columnName = JdbcUtils.lookupColumnName(metaData, columnIndex);
		return columnName.toLowerCase(Locale.US);
	}

	private int findParameterIndex(String[] parameterNames, String columnName) {
		return Arrays.asList(parameterNames).indexOf(columnName);
	}

	private Object[] getArguments(ResultSet rs, int[] columnIndexes, Class<?>[] parameterTypes) throws SQLException {
		int parameterCount = parameterTypes.length;
		Object[] arguments = new Object[parameterCount];
		for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++) {
			int columnIndex = columnIndexes[parameterIndex];
			if (columnIndex != 0) {
				Class<?> parameterType = parameterTypes[parameterIndex];
				Object argument = JdbcUtils.getResultSetValue(rs, columnIndex, parameterType);
				arguments[parameterIndex] = argument;
			}
		}
		return arguments;
	}

	private static <T> Constructor<T> findConstructor(Class<T> rowClass) {
		Constructor<?>[] constructors = rowClass.getConstructors();

		Constructor<?> annotatedConstructor = null;
		for (Constructor<?> constructor : constructors) {
			ConstructorProperties annotation = constructor.getAnnotation(ConstructorProperties.class);
			if (annotation == null) {
				continue;
			}

			checkArgument(
					annotatedConstructor == null,
					"rowClass %s has more than one constructor annotated with @ConstructorProperties",
					rowClass);

			annotatedConstructor = constructor;
		}

		checkArgument(
				annotatedConstructor != null,
				"rowClass %s has no constructors annotated with @ConstructorProperties",
				rowClass);

		@SuppressWarnings("unchecked")
		Constructor<T> constructor = (Constructor<T>) annotatedConstructor;
		return constructor;
	}

}
