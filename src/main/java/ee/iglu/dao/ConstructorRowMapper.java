package ee.iglu.dao;

import static com.google.common.base.Preconditions.checkArgument;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import com.google.common.base.CaseFormat;
import com.google.common.base.Throwables;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstructorRowMapper<T> implements RowMapper<T> {

	public ConstructorRowMapper(Class<T> rowClass) {
		this(findConstructor(rowClass));
	}

	private ConstructorRowMapper(Constructor<T> constructor) {
		this(
				constructor,
				constructor.getParameterTypes(),
				constructor.getAnnotation(ConstructorProperties.class).value());
	}

	private final Constructor<T> constructor;
	private final Class<?>[] parameterTypes;
	private final String[] parameterNames;

	private int[] columnIndexes;

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		if (columnIndexes == null) {
			columnIndexes = createParameterToColumnMap(rs.getMetaData(), parameterNames);
		}

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

		BitSet mappedParameters = new BitSet();
		BitSet mappedColumns = new BitSet();

		int[] columnIndexes = new int[parameterCount];
		for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
			String parameterName = getParameterNameForColumn(metaData, columnIndex);
			int parameterIndex = findParameterIndex(parameterNames, parameterName);
			if (parameterIndex != -1) {
				columnIndexes[parameterIndex] = columnIndex;
				mappedParameters.set(parameterIndex);
				mappedColumns.set(columnIndex);
			}
		}

		checkFullyPopulated(mappedParameters, parameterCount, mappedColumns, columnCount, metaData);

		return columnIndexes;
	}

	private void checkFullyPopulated(
			BitSet mappedParameters,
			int parameterCount,
			BitSet mappedColumns,
			int columnCount,
			ResultSetMetaData metaData) throws SQLException {

		int mappedParameterCount = mappedParameters.cardinality();
		int mappedColumnCount = mappedColumns.cardinality();
		if (mappedParameterCount == parameterCount && mappedColumnCount == columnCount) {
			return;
		}

		List<String> missingParameters = new ArrayList<>(parameterCount - mappedParameterCount);
		for (int i = mappedParameters.nextClearBit(0); i < parameterCount; i = mappedParameters.nextClearBit(i + 1)) {
			missingParameters.add(parameterNames[i]);
		}

		List<String> excessColumns = new ArrayList<>(columnCount - mappedColumnCount);
		for (int i = mappedColumns.nextClearBit(1); i <= columnCount; i = mappedColumns.nextClearBit(i + 1)) {
			excessColumns.add(getParameterNameForColumn(metaData, i));
		}

		String msg = String.format("%s: no 1-to-1 mapping from ResultSet", constructor.getDeclaringClass().getSimpleName());

		if (!missingParameters.isEmpty()) {
			msg += String.format(", missing properties: %s", missingParameters);
		}

		if (!excessColumns.isEmpty()) {
			msg += String.format(", excess columns: %s", excessColumns);
		}

		throw new IllegalStateException(msg);
	}

	private String getParameterNameForColumn(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		String columnName = JdbcUtils.lookupColumnName(metaData, columnIndex);
		String lowerUnderscore = columnName.toLowerCase(Locale.US);
		String lowerCamel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, lowerUnderscore);
		return lowerCamel;
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
