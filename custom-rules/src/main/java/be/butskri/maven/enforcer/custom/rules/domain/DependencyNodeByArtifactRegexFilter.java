package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Predicate;

public class DependencyNodeByArtifactRegexFilter implements Predicate<DependencyNode> {

	private Set<String> artifactNameRegExPatterns;

	public DependencyNodeByArtifactRegexFilter(Set<String> dependencyFilterSet) {
		this.artifactNameRegExPatterns = toRegExPatterns(dependencyFilterSet);
	}

	public boolean apply(DependencyNode dependencyNode) {
		return allows(dependencyNode.getFullMavenArtifactId().getMavenArtifactId().toString());
	}

	private boolean allows(String fullArtifactName) {
		for (String regex : artifactNameRegExPatterns) {
			if (fullArtifactName.matches(regex)) {
				return true;
			}
		}
		return false;
	}

	private Set<String> toRegExPatterns(Set<String> dependencyFilterSet) {
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
