package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

public class MavenArtifactIdRegexFilter implements Predicate<MavenArtifactId> {

	private static final HashSet<String> DEFAULT_INCLUDES_SET = Sets.newHashSet(".*");
	private static final HashSet<String> DEFAULT_EXCLUDES_SET = Sets.newHashSet();
	private Set<String> regexIncludes;
	private Set<String> regexExcludes;

	public MavenArtifactIdRegexFilter(Set<String> regexIncludes, Set<String> regexExcludes) {
		this.regexIncludes = toRegExPatterns(regexIncludes, DEFAULT_INCLUDES_SET);
		this.regexExcludes = toRegExPatterns(regexExcludes, DEFAULT_EXCLUDES_SET);
	}

	public boolean apply(MavenArtifactId artifactId) {
		return allows(artifactId.toString());
	}

	private boolean allows(String fullArtifactName) {
		return isAllowedBy(regexIncludes, fullArtifactName) && !isAllowedBy(regexExcludes, fullArtifactName);
	}

	private boolean isAllowedBy(Set<String> regexes, String fullArtifactName) {
		for (String regex : regexes) {
			if (fullArtifactName.matches(regex)) {
				return true;
			}
		}
		return false;
	}

	private Set<String> toRegExPatterns(Set<String> dependencyFilterSet, HashSet<String> defaultRegexPatterns) {
		if (dependencyFilterSet == null || dependencyFilterSet.isEmpty()) {
			return defaultRegexPatterns;
		}
		Set<String> result = new HashSet<String>();
		for (String dependencyFilter : dependencyFilterSet) {
			result.add(toRegExPattern(dependencyFilter));
		}
		return result;
	}

	private String toRegExPattern(String dependencyFilter) {
		return dependencyFilter.replace(".", "\\.").replace("*", ".*").concat(".*");
	}

}
