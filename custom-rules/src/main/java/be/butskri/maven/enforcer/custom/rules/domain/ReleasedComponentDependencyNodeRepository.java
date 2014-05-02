package be.butskri.maven.enforcer.custom.rules.domain;

import static be.butskri.maven.enforcer.custom.rules.domain.FullMavenArtifactId.fullMavenArtifactIdFrom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;

public class ReleasedComponentDependencyNodeRepository implements DependencyNodeRepository {

	private DependencyRepository dependencyRepository;

	public ReleasedComponentDependencyNodeRepository(DependencyRepository dependencyRepository) {
		this.dependencyRepository = dependencyRepository;
	}

	public List<DependencyNode> findDependentComponentsFor(DependencyNode parentNode) {
		return dependentComponentsFromReleasedComponent(parentNode);
	}

	private List<DependencyNode> dependentComponentsFromReleasedComponent(DependencyNode parentNode) {
		List<DependencyNode> result = new ArrayList<DependencyNode>();
		Collection<Dependency> dependencies = dependencyRepository.getDependenciesFor(parentNode.getFullMavenArtifactId());
		for (Dependency dependency : dependencies) {
			result.add(new DependencyNode(fullMavenArtifactIdFrom(dependency), dependency.getScope(),
					exclusions(dependency)));
		}
		return result;
	}

	private Set<SimpleArtifactId> exclusions(Dependency dependency) {
		Set<SimpleArtifactId> result = new HashSet<SimpleArtifactId>();
		for (Exclusion exclusion : dependency.getExclusions()) {
			result.add(new SimpleArtifactId(exclusion.getGroupId(), exclusion.getArtifactId()));
		}
		return result;
	}

}
