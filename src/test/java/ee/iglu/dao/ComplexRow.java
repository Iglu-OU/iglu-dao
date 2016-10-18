package ee.iglu.dao;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComplexRow {
	private Integer id;
	private String text;
	private boolean multiPartName;
	private UUID uuid;
}
