package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class MavenArtifactId {

	private static final Pattern PATTERN = Pattern.compile("([^:]*):([^:]*):([^:]*)");
	private String groupId;
	private String artifactId;
	private String type;

	public static MavenArtifactId fromString(String string) {
		Matcher matcher = PATTERN.matcher(string);
		matcher.find();
		if (!matcher.matches() || !matcher.hitEnd()) {
			throw new IllegalArgumentException(String.format("artifactId %s matcht niet met pattern (groupId:artifactId:type)", string));
		}
		return new MavenArtifactId(matcher.group(1), matcher.group(2), matcher.group(3));
	}

	public MavenArtifactId(String groupId, String artifactId, String type) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.type = type;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return new StringBuilder().append(groupId).append(":").append(artifactId).append(":").append(type).toString();
	}
}
