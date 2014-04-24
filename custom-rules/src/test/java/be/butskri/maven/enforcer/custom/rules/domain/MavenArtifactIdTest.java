package be.butskri.maven.enforcer.custom.rules.domain;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class MavenArtifactIdTest {

	@Test
	public void constructorMetEenParameterParsedString() {
		MavenArtifactId fullMavenArtifactId = MavenArtifactId.fromString("my.groupId:my.artifactId:myType");

		assertThat(fullMavenArtifactId.getGroupId()).isEqualTo("my.groupId");
		assertThat(fullMavenArtifactId.getArtifactId()).isEqualTo("my.artifactId");
		assertThat(fullMavenArtifactId.getType()).isEqualTo("myType");
	}

	@Test
	public void constructorGooitExceptionIndienStringPatternNietMatcht() {
		try {
			MavenArtifactId.fromString("my.groupId:my.artifactId:myType:somethingElse");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void constructorGooitExceptionIndienStringPatternNietMatcht2() {
		try {
			MavenArtifactId.fromString("my.groupId:my.artifactId");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

}
