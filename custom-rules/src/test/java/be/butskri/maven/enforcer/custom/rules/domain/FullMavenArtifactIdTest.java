package be.butskri.maven.enforcer.custom.rules.domain;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class FullMavenArtifactIdTest {

	@Test
	public void constructorMetEenParameterParsedString() {
		FullMavenArtifactId fullMavenArtifactId = FullMavenArtifactId.fromString("my.groupId:my.artifactId:myType:myVersion");

		assertThat(fullMavenArtifactId.getMavenArtifactId().getGroupId()).isEqualTo("my.groupId");
		assertThat(fullMavenArtifactId.getMavenArtifactId().getArtifactId()).isEqualTo("my.artifactId");
		assertThat(fullMavenArtifactId.getMavenArtifactId().getType()).isEqualTo("myType");
		assertThat(fullMavenArtifactId.getVersion()).isEqualTo("myVersion");
	}

	@Test
	public void constructorGooitExceptionIndienStringPatternNietMatcht() {
		try {
			FullMavenArtifactId.fromString("my.groupId:my.artifactId:myType:myVersion:additionalStuff");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void constructorGooitExceptionIndienStringPatternNietMatcht2() {
		try {
			FullMavenArtifactId.fromString("my.groupId:my.artifactId:myType");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

}
