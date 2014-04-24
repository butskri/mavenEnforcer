package be.butskri.maven.enforcer.custom.rules.appender;

import org.apache.maven.plugin.logging.Log;

public class LoggingStringAppender implements StringAppender {
	private LogLevel logLevel;
	private Log log;

	public LoggingStringAppender(Log log, LogLevel logLevel) {
		this.logLevel = logLevel;
		this.log = log;
	}

	public void append(String string) {
		logLevel.log(log, string);
	}

}
