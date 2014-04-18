package be.butskri.maven.enforcer.custom.rules;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import be.butskri.maven.enforcer.custom.rules.ArtifactNameFilter;

public class ArtifactFilterTest {

	@Test
	public void artifactFilterAllowsGeeftAlleenTrueTerugIndienPatternMatcht() {
		assertThat(artifactNameFilter("be.argenta").allows("be.argenta.component:core-component:jar")).isTrue();
		assertThat(artifactNameFilter("be.argenta").allows("com.argenta.component:core-component:jar")).isFalse();
	}

	@Test
	public void artifactFilterAllowsGeeftTrueTerugIndienMinstensEenPatternMatcht() {
		assertThat(artifactNameFilter("be.argenta", "com.argenta").allows("net.argenta.component:core-component:jar")).isFalse();
		assertThat(artifactNameFilter("be.argenta", "com.argenta").allows("com.argenta.component:core-component:jar")).isTrue();
	}

	@Test
	public void puntWordtGeEscaped() {
		assertThat(artifactNameFilter("be.argenta.component:core-component:jar").allows("be.argenta.component:core-component:jar"))
				.isTrue();
		assertThat(artifactNameFilter("be.argenta.component:core-component:jar").allows("be.argentahcomponent:core-component:jar"))
				.isFalse();
	}

	@Test
	public void artifactFilterKanGebruikMakenVanSterrekeAlsWildCard() {
		assertThat(artifactNameFilter("be.argenta*:core-component:jar").allows("com.argenta.component:core-component:jar")).isFalse();
		assertThat(artifactNameFilter("be.argenta*:core-*:jar").allows("be.argenta.component:score-component:jar")).isFalse();
		assertThat(artifactNameFilter("be.argenta*:core-component:jar").allows("com.argenta.component:core-component:jar")).isFalse();

		assertThat(artifactNameFilter("be.argenta*").allows("be.argenta.component:core-component:jar")).isTrue();
		assertThat(artifactNameFilter("be.argenta*:core-component:jar").allows("be.argenta.component:core-component:jar")).isTrue();
		assertThat(artifactNameFilter("be.argenta*:core-*:jar").allows("be.argenta.component:core-component:jar")).isTrue();
		assertThat(artifactNameFilter("be.argenta*:core-*:*").allows("be.argenta.component:core-component:jar")).isTrue();
	}

	private ArtifactNameFilter artifactNameFilter(String... allowedPatterns) {
		return new ArtifactNameFilter(toSet(allowedPatterns));
	}

	private Set<String> toSet(String... allowedPatterns) {
		Set<String> result = new HashSet<String>();
		for (String pattern : allowedPatterns) {
			result.add(pattern);
		}
		return result;
	}
}
