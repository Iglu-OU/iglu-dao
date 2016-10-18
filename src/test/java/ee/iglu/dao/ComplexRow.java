package ee.iglu.dao;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComplexRow {
	private CustomWrapper id;
	private String text;
	private boolean multiPartName;
	private UUID uuid;
	private Instant instant;
}
