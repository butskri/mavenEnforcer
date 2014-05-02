package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SimpleArtifactId {

	private static final Pattern PATTERN = Pattern.compile("([^:]*):([^:]*)");
	private String groupId;
	private String artifactId;

	public static SimpleArtifactId fromString(String string) {
		Matcher matcher = PATTERN.matcher(string);
		matcher.find();
		if (!matcher.matches() || !matcher.hitEnd()) {
			throw new IllegalArgumentException(String.format("artifactId %s matcht niet met pattern (groupId:artifactId)", string));
		}
		return new SimpleArtifactId(matcher.group(1), matcher.group(2));
	}

	public SimpleArtifactId(String groupId, String artifactId) {
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return String.format("%s:%s", groupId, artifactId);
	}

}
