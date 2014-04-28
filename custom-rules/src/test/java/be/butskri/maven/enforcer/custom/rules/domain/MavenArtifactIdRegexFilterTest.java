package be.butskri.maven.enforcer.custom.rules.domain;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class MavenArtifactIdRegexFilterTest {

	@Test
	public void artifactFilterAltijdTrueTerugIndienGeenIncludesEnGeenExcludesPatterns() {
		MavenArtifactIdRegexFilter filter = createFilter(including(), excluding());
		assertThat(filter.apply(artifactId("something:core-component:jar"))).isTrue();
		assertThat(filter.apply(artifactId("somethingeldse:core-component:jar"))).isTrue();
	}

	@Test
	public void artifactFilterGeeftFalseTerugIndienComponentInEenVanDeExcludesPatterns() {
		MavenArtifactIdRegexFilter filter = createFilter(including("be.butskri"), excluding("be.butskri.excluded"));
		assertThat(filter.apply(artifactId("be.butskri.excluded:core-component:jar"))).isFalse();
		assertThat(filter.apply(artifactId("be.butskri.included:core-component:jar"))).isTrue();
		assertThat(filter.apply(artifactId("com.butskri.included:core-component:jar"))).isFalse();
		assertThat(filter.apply(artifactId("com.butskri.excluded:core-component:jar"))).isFalse();
	}

	@Test
	public void artifactFilterAllowsGeeftAlleenTrueTerugIndienPatternMatcht() {
		MavenArtifactIdRegexFilter filter = createFilter(including("be.butskri"), excluding());
		assertThat(filter.apply(artifactId("be.butskri.component:core-component:jar"))).isTrue();
		assertThat(filter.apply(artifactId("com.butskri.component:core-component:jar"))).isFalse();
	}

	@Test
	public void artifactFilterAllowsGeeftTrueTerugIndienMinstensEenPatternMatcht() {
		MavenArtifactIdRegexFilter filter = createFilter(including("be.butskri", "com.butskri"), excluding());

		assertThat(filter.apply(artifactId("be.butskri.component:core-component:jar"))).isTrue();
		assertThat(filter.apply(artifactId("net.butskri.component:core-component:jar"))).isFalse();
		assertThat(filter.apply(artifactId("com.butskri.component:core-component:jar"))).isTrue();
	}

	@Test
	public void puntWordtGeEscaped() {
		MavenArtifactIdRegexFilter filter = createFilter("be.butskri.component:core-component:jar");

		assertThat(filter.apply(artifactId("be.butskri.component:core-component:jar"))).isTrue();
		assertThat(filter.apply(artifactId("be.butskrihcomponent:core-component:jar"))).isFalse();
	}

	@Test
	public void artifactFilterKanGebruikMakenVanSterrekeAlsWildCard() {
		assertThat(createFilter("be.butskri*:core-component:jar").apply(artifactId("com.butskri.component:core-component:jar"))).isFalse();
		assertThat(createFilter("be.butskri*:core-*:jar").apply(artifactId("be.butskri.component:score-component:jar"))).isFalse();
		assertThat(createFilter("be.butskri*:core-component:jar").apply(artifactId("com.butskri.component:core-component:jar"))).isFalse();

		assertThat(createFilter("be.butskri*").apply(artifactId("be.butskri.component:core-component:jar"))).isTrue();
		assertThat(createFilter("be.butskri*:core-component:jar").apply(artifactId("be.butskri.component:core-component:jar"))).isTrue();
		assertThat(createFilter("be.butskri*:core-*:jar").apply(artifactId("be.butskri.component:core-component:jar"))).isTrue();
		assertThat(createFilter("be.butskri*:core-*:*").apply(artifactId("be.butskri.component:core-component:jar"))).isTrue();
	}

	private MavenArtifactIdRegexFilter createFilter(String... allowedPatterns) {
		return new MavenArtifactIdRegexFilter(toSet(allowedPatterns), new HashSet<String>());
	}

	private MavenArtifactIdRegexFilter createFilter(Set<String> includesPatterns, Set<String> excludesPatterns) {
		return new MavenArtifactIdRegexFilter(includesPatterns, excludesPatterns);
	}

	private Set<String> including(String... includesPatterns) {
		return toSet(includesPatterns);
	}

	private Set<String> excluding(String... excludesPatterns) {
		return toSet(excludesPatterns);
	}

	private Set<String> toSet(String... allowedPatterns) {
		Set<String> result = new HashSet<String>();
		for (String pattern : allowedPatterns) {
			result.add(pattern);
		}
		return result;
	}

	private MavenArtifactId artifactId(String mavenArtifactId) {
		return MavenArtifactId.fromString(mavenArtifactId);
	}
}
