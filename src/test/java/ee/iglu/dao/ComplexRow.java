package ee.iglu.dao;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplexRow {
	private CustomWrapper id;
	private String text;
	private boolean multiPartName;
	private UUID uuid;
	private Instant instant;
}
