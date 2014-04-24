package be.butskri.maven.enforcer.custom.rules.appender;

import org.apache.maven.plugin.logging.Log;

public enum LogLevel {
	DEBUG {
		@Override
		void log(Log log, String message) {
			log.debug(message);
		}
	},
	INFO {
		@Override
		void log(Log log, String message) {
			log.info(message);
		}
	},
	WARN {
		@Override
		void log(Log log, String message) {
			log.warn(message);
		}
	},
	ERROR {
		@Override
		void log(Log log, String message) {
			log.error(message);
		}
	};

	abstract void log(Log log, String message);
}
