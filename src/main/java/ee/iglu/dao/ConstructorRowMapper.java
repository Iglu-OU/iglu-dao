package ee.iglu.dao;

import static com.google.common.base.Preconditions.checkArgument;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Throwables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

@Slf4j
@RequiredArgsConstructor
public class ConstructorRowMapper<T> implements RowMapper<T> {
	private final Class<T> rowClass;

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		log.debug("mapping row nr {}", rowNum);

		Constructor<T> constructor = findConstructor(rowClass);
		Class<?>[] parameterTypes = constructor.getParameterTypes();

		Object[] arguments = new Object[parameterTypes.length];

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
