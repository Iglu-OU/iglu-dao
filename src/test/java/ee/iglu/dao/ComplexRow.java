package ee.iglu.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComplexRow {
	private Integer id;
	private String text;
	private boolean multiPartName;
}
