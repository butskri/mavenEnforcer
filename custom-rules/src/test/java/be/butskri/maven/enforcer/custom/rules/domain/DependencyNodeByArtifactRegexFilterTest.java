package be.butskri.maven.enforcer.custom.rules.domain;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DependencyNodeByArtifactRegexFilterTest {

	@Test
	public void artifactFilterAllowsGeeftAlleenTrueTerugIndienPatternMatcht() {
		assertThat(createFilter("be.argenta").apply(node("be.argenta.component:core-component:jar"))).isTrue();
		assertThat(createFilter("be.argenta").apply(node("com.argenta.component:core-component:jar"))).isFalse();
	}

	@Test
	public void artifactFilterAllowsGeeftTrueTerugIndienMinstensEenPatternMatcht() {
		assertThat(createFilter("be.argenta", "com.argenta").apply(node("net.argenta.component:core-component:jar"))).isFalse();
		assertThat(createFilter("be.argenta", "com.argenta").apply(node("com.argenta.component:core-component:jar"))).isTrue();
	}

	@Test
	public void puntWordtGeEscaped() {
		assertThat(createFilter("be.argenta.component:core-component:jar").apply(node("be.argenta.component:core-component:jar")))
				.isTrue();
		assertThat(createFilter("be.argenta.component:core-component:jar").apply(node("be.argentahcomponent:core-component:jar")))
				.isFalse();
	}

	@Test
	public void artifactFilterKanGebruikMakenVanSterrekeAlsWildCard() {
		assertThat(createFilter("be.argenta*:core-component:jar").apply(node("com.argenta.component:core-component:jar"))).isFalse();
		assertThat(createFilter("be.argenta*:core-*:jar").apply(node("be.argenta.component:score-component:jar"))).isFalse();
		assertThat(createFilter("be.argenta*:core-component:jar").apply(node("com.argenta.component:core-component:jar"))).isFalse();

		assertThat(createFilter("be.argenta*").apply(node("be.argenta.component:core-component:jar"))).isTrue();
		assertThat(createFilter("be.argenta*:core-component:jar").apply(node("be.argenta.component:core-component:jar"))).isTrue();
		assertThat(createFilter("be.argenta*:core-*:jar").apply(node("be.argenta.component:core-component:jar"))).isTrue();
		assertThat(createFilter("be.argenta*:core-*:*").apply(node("be.argenta.component:core-component:jar"))).isTrue();
	}

	private DependencyNodeByArtifactRegexFilter createFilter(String... allowedPatterns) {
		return new DependencyNodeByArtifactRegexFilter(toSet(allowedPatterns));
	}

	private Set<String> toSet(String... allowedPatterns) {
		Set<String> result = new HashSet<String>();
		for (String pattern : allowedPatterns) {
			result.add(pattern);
		}
		return result;
	}

	private DependencyNode node(String mavenArtifactId) {
		return new DependencyNode(new FullMavenArtifactId(MavenArtifactId.fromString(mavenArtifactId), "1.0.0"),
				new HashSet<MavenArtifactId>());
	}
}
