package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.Set;

import com.google.common.base.Predicate;

public class DependencyNodeByArtifactRegexFilter implements Predicate<DependencyNode> {

	private MavenArtifactIdRegexFilter mavenArtifactIdRegexFilter;

	public static DependencyNodeByArtifactRegexFilter dependencyNodeByArtifactRegexFilter(Set<String> regexIncludes,
			Set<String> regexExcludes) {
		return new DependencyNodeByArtifactRegexFilter(new MavenArtifactIdRegexFilter(regexIncludes, regexExcludes));
	}

	DependencyNodeByArtifactRegexFilter(MavenArtifactIdRegexFilter mavenArtifactIdRegexFilter) {
		this.mavenArtifactIdRegexFilter = mavenArtifactIdRegexFilter;
	}

	public boolean apply(DependencyNode dependencyNode) {
		return mavenArtifactIdRegexFilter.apply(dependencyNode.getFullMavenArtifactId().getMavenArtifactId());
	}

}
