package be.butskri.maven.enforcer.custom.rules.appender;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class CollectingStringAppenderTest {

	private static final String STRING3 = "yetAnotherString";
	private static final String STRING2 = "someOtherString";
	private static final String STRING1 = "someString";

	@Test
	public void appendAddsStringToListOfCollectedStrings() {
		CollectingStringAppender appender = new CollectingStringAppender();
		appender.append(STRING1);
		appender.append(STRING2);
		appender.append(STRING3);

		assertThat(appender.getCollectedStrings()).containsExactly(STRING1, STRING2, STRING3);
	}

}
