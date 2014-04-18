package be.butskri.maven.enforcer.custom.rules;

import java.util.HashSet;
import java.util.Set;

public class ArtifactNameFilter {

	private Set<String> artifactNameRegExPatterns;

	public ArtifactNameFilter(Set<String> dependencyFilterSet) {
		this.artifactNameRegExPatterns = toRegExPatterns(dependencyFilterSet);
	}

	public boolean allows(String fullArtifactName) {
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
