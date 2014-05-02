package be.butskri.maven.enforcer.custom.rules.domain;

import static be.butskri.maven.enforcer.custom.rules.domain.MavenArtifactId.mavenArtifactIdFrom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.graph.Dependency;

public class FullMavenArtifactId {

	private static final Pattern PATTERN = Pattern.compile("([^:]*):([^:]*):([^:]*):([^:]*)");

	private MavenArtifactId mavenArtifactId;
	private String version;

	public static FullMavenArtifactId fromString(String string) {
		Matcher matcher = PATTERN.matcher(string);
		matcher.find();
		if (!matcher.matches() || !matcher.hitEnd()) {
			throw new IllegalArgumentException(String.format("artifactId %s matcht niet met pattern (groupId:artifactId:type)", string));
		}
		MavenArtifactId mavenArtifactId = new MavenArtifactId(matcher.group(1), matcher.group(2), matcher.group(3));
		return new FullMavenArtifactId(mavenArtifactId, matcher.group(4));
	}

	public static FullMavenArtifactId fullMavenArtifactIdFrom(MavenProject aMavenProject) {
		return new FullMavenArtifactId(new MavenArtifactId(aMavenProject.getGroupId(), aMavenProject.getArtifactId(),
				aMavenProject.getPackaging()), aMavenProject.getVersion());
	}

	public static FullMavenArtifactId fullMavenArtifactIdFrom(Artifact artifact) {
		return new FullMavenArtifactId(mavenArtifactIdFrom(artifact), artifact.getVersion());
	}

	public static FullMavenArtifactId fullMavenArtifactIdFrom(Dependency dependency) {
		return new FullMavenArtifactId(new MavenArtifactId(dependency.getArtifact().getGroupId(), dependency.getArtifact().getArtifactId(),
				dependency.getArtifact().getExtension()), dependency.getArtifact().getVersion());
	}

	public FullMavenArtifactId(MavenArtifactId mavenArtifactId, String version) {
		this.mavenArtifactId = mavenArtifactId;
		this.version = version;
	}

	public MavenArtifactId getMavenArtifactId() {
		return mavenArtifactId;
	}

	public String getVersion() {
		return version;
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
		return new StringBuilder().append(mavenArtifactId.toString()).append(":").append(version).toString();
	}

	public boolean hasRangedVersion() {
		return version.contains("[") || version.contains("(") || version.contains("]") || version.contains(")");
	}

}
