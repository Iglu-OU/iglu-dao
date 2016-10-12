package ee.iglu.dao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SimpleRow {
	private final Long id;
	private final String text;
}
