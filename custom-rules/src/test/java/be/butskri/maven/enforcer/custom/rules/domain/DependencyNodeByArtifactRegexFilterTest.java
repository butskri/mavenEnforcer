package be.butskri.maven.enforcer.custom.rules.domain;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class DependencyNodeByArtifactRegexFilterTest {

	private MavenArtifactIdRegexFilter artifactIdRegexFilterMock;
	private DependencyNodeByArtifactRegexFilter filter;

	@Before
	public void setUp() {
		artifactIdRegexFilterMock = mock(MavenArtifactIdRegexFilter.class);
		filter = new DependencyNodeByArtifactRegexFilter(artifactIdRegexFilterMock);
	}

	@Test
	public void artifactFilterAllowsGeeftTrueTerugIndienPatternMatcht() {
		MavenArtifactId artifactId = MavenArtifactId.fromString("be.argenta.component:core-component:jar");
		when(artifactIdRegexFilterMock.apply(artifactId)).thenReturn(true);

		assertThat(filter.apply(node(artifactId))).isTrue();
	}

	@Test
	public void artifactFilterAllowsGeeftFalseTerugIndienPatternNietMatcht() {
		MavenArtifactId artifactId = MavenArtifactId.fromString("be.argenta.component:core-component:jar");
		when(artifactIdRegexFilterMock.apply(artifactId)).thenReturn(false);

		assertThat(filter.apply(node(artifactId))).isFalse();
	}

	private DependencyNode node(MavenArtifactId mavenArtifactId) {
		return new DependencyNode(new FullMavenArtifactId(mavenArtifactId, "1.0.0"),
				new HashSet<MavenArtifactId>());
	}
}
