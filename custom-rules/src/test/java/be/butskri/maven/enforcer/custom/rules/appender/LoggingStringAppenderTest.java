package be.butskri.maven.enforcer.custom.rules.appender;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mockito.InOrder;

public class LoggingStringAppenderTest {

	private static final String STRING3 = "yetAnotherString";
	private static final String STRING2 = "someOtherString";
	private static final String STRING1 = "someString";

	@Test
	public void appendLogsString() {
		Log logMock = mock(Log.class);
		LoggingStringAppender appender = new LoggingStringAppender(logMock, LogLevel.INFO);
		appender.append(STRING1);
		appender.append(STRING2);
		appender.append(STRING3);

		InOrder inOrder = inOrder(logMock);
		inOrder.verify(logMock).info(STRING1);
		inOrder.verify(logMock).info(STRING2);
		inOrder.verify(logMock).info(STRING3);
		verifyNoMoreInteractions(logMock);
	}

}
