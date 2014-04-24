package be.butskri.maven.enforcer.custom.rules.appender;

import java.util.ArrayList;
import java.util.List;

public class CollectingStringAppender implements StringAppender {

	private List<String> result = new ArrayList<String>();

	public void append(String string) {
		result.add(string);
	}

	public List<String> getCollectedStrings() {
		return result;
	}
}
