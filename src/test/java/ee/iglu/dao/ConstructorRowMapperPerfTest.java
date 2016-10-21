package ee.iglu.dao;

import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@Sql("/ee/iglu/dao/ConstructorRowMapperTest.sql")
@RunWith(SpringRunner.class)
@SpringBootTest
public class ConstructorRowMapperPerfTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@Ignore // un-ignore and run manually
	public void test_simple_row_performance() {

		String query = "SELECT * FROM simple";
		int iterationsPerSample = 700;
		int sampleSize = 10;
		measureRowMappersWithQuery(
				query,
				iterationsPerSample,
				sampleSize,
				new CustomSimpleRowMapper(),
				new ConstructorRowMapper<>(SimpleRow.class),
				new BeanPropertyRowMapper<>(SimpleRow.class));
	}

	@Test
	//	@Ignore // un-ignore and run manually
	public void test_complex_row_performance() {
		String query = "SELECT * FROM complex";
		int iterationsPerSample = 50000;
		int sampleSize = 10;
		measureRowMappersWithQuery(
				query,
				iterationsPerSample,
				sampleSize,
				new CustomComplexRowMapper(),
				new ConstructorRowMapper<>(ComplexRow.class)
				//				,new BeanPropertyRowMapper<>(ComplexRow.class)
		);
	}

	private void measureRowMappersWithQuery(
			String query,
			int iterationsPerSample,
			int sampleSize,
			RowMapper<?>... rowMappers) {

		double[] results = new double[rowMappers.length];
		for (int i = 0; i < rowMappers.length; i++) {
			results[i] = measure(query, iterationsPerSample, sampleSize, rowMappers[i]);
		}

		log.info("{}: {}ms",
				rowMappers[0].getClass().getSimpleName(),
				results[0]);

		for (int i = 1; i < results.length; i++) {
			log.info("{}: {}ms, {} compared to {}",
					rowMappers[i].getClass().getSimpleName(),
					results[i],
					formatPercent(results[0], results[i]),
					rowMappers[0].getClass().getSimpleName());
		}
	}

	private double measure(String query, int iterationsPerSample, int sampleSize, RowMapper<?> rowMapper) {
		log.info("measuring {}", rowMapper.getClass().getSimpleName());

		List<Integer> samples = new ArrayList<>();
		Stopwatch stopwatch = Stopwatch.createStarted();
		for (int i = 1; i <= iterationsPerSample * sampleSize * 10; i++) {
			jdbcTemplate.query(query, rowMapper);

			if (i % iterationsPerSample == 0) {
				long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
				samples.add((int) elapsed);
				double average = getAverage(samples, sampleSize);
				if (average >= 0) {
					return average;
				}
				stopwatch.reset().start();
			}
		}

		fail("measurement was not accurate, please rerun the test");
		return -1;
	}

	private double getAverage(List<Integer> samples, int sampleSize) {
		int sampleCount = samples.size();
		int lastSampleValue = samples.get(sampleCount - 1);
		int skip = sampleCount - sampleSize;
		if (skip < 0) {
			log.info("sample {} - {}ms", sampleCount, lastSampleValue);
			return -1;
		}

		double average = samples.stream()
				.skip(skip)
				.mapToInt(Integer::intValue)
				.average().getAsDouble();
		double variance = samples.stream()
				.skip(skip)
				.mapToDouble(elapsed -> (elapsed - average) * (elapsed - average))
				.average().getAsDouble();
		double standardDeviation = Math.sqrt(variance);
		double relativeStandardDeviation = standardDeviation / average * 100;

		log.info("sample {} - {}ms, avg of last {} samples: {}ms, std dev: {}ms ({}%)",
				sampleCount,
				lastSampleValue,
				sampleSize,
				average,
				String.format("%.1f", standardDeviation),
				String.format("%.1f", relativeStandardDeviation));

		if (relativeStandardDeviation >= 1.2) {
			return -1;
		}

		return average;
	}

	private String formatPercent(Double base, double target) {
		if (base == null) {
			return "0.0%";
		}
		double ratio = target / base;
		double percent = (ratio - 1) * 100;
		return String.format("%+.1f%%", percent);
	}

	private static class CustomSimpleRowMapper implements RowMapper<SimpleRow> {
		@Override
		public SimpleRow mapRow(ResultSet rs, int rowNum) throws SQLException {
			Long id = (Long) JdbcUtils.getResultSetValue(rs, 1, Long.class);
			String text = (String) JdbcUtils.getResultSetValue(rs, 2, String.class);
			return new SimpleRow(id, text);
		}
	}

	private static class CustomComplexRowMapper implements RowMapper<ComplexRow> {

		@Override
		public ComplexRow mapRow(ResultSet rs, int rowNum) throws SQLException {
			Long idValue = (Long) JdbcUtils.getResultSetValue(rs, 1, Long.class);
			String text = (String) JdbcUtils.getResultSetValue(rs, 2, String.class);
			boolean multiPartName = (boolean) JdbcUtils.getResultSetValue(rs, 3, boolean.class);
			String uuidValue = (String) JdbcUtils.getResultSetValue(rs, 4, String.class);
			Timestamp timestamp = (Timestamp) JdbcUtils.getResultSetValue(rs, 5, Timestamp.class);

			return new ComplexRow(
					new CustomWrapper(idValue),
					text,
					multiPartName,
					UUID.fromString(uuidValue),
					timestamp.toInstant()
			);
		}
	}
}
