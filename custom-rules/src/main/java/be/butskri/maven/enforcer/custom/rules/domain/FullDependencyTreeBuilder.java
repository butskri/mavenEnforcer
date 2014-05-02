package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.HashSet;
import java.util.List;

import org.apache.maven.project.MavenProject;

import com.google.common.base.Predicate;

public class FullDependencyTreeBuilder {

	private MavenProject mavenProject;
	private DependencyNodeRepository dependencyNodeRepository;
	private final Predicate<MavenArtifactId> artifactsFilter;

	public FullDependencyTreeBuilder(MavenProject mavenProject, DependencyNodeRepository dependencyNodeRepository,
			Predicate<MavenArtifactId> artifactsFilter) {
		this.mavenProject = mavenProject;
		this.dependencyNodeRepository = dependencyNodeRepository;
		this.artifactsFilter = artifactsFilter;
	}

	public FullDependencyTree build() {
		return new FullDependencyTree(buildRootNode());
	}

	private DependencyNode buildRootNode() {
		DependencyNode rootNode = new DependencyNode(FullMavenArtifactId.fullMavenArtifactIdFrom(mavenProject), "noScope",
				new HashSet<SimpleArtifactId>());
		addDependentComponentsNew(rootNode);
		return rootNode;
	}

	private void addDependentComponentsNew(DependencyNode parentNode) {
		List<DependencyNode> dependentComponents = dependencyNodeRepository.findDependentComponentsFor(parentNode);
		for (DependencyNode dependentComponent : dependentComponents) {
			if (shouldAddDependency(dependentComponent)) {
				parentNode.withDependentComponent(dependentComponent);
				if (shouldAddNestedDependenciesFor(dependentComponent)) {
					addDependentComponentsNew(dependentComponent);
				}
			}
		}
	}

	private boolean shouldAddDependency(DependencyNode childNode) {
		return artifactsFilter.apply(childNode.getFullMavenArtifactId().getMavenArtifactId()) && !"test".equals(childNode.getScope());
	}

	private boolean shouldAddNestedDependenciesFor(DependencyNode dependentComponent) {
		return !dependentComponent.isExcluded() && !dependentComponent.isRecursive()
				&& !dependentComponent.getFullMavenArtifactId().hasRangedVersion();
	}

}
