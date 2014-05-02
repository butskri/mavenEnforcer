package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;

import com.google.common.base.Predicate;

public class FullDependencyTreeBuilder {

	private MavenProject mavenProject;
	private DependencyRepository dependencyRepository;
	private EnforcerRuleHelper helper;
	private final Predicate<MavenArtifactId> artifactsFilter;

	public FullDependencyTreeBuilder(MavenProject mavenProject, EnforcerRuleHelper helper, Predicate<MavenArtifactId> artifactsFilter) {
		this.mavenProject = mavenProject;
		this.dependencyRepository = DependencyRepository.getRepository(helper);
		this.helper = helper;
		this.artifactsFilter = artifactsFilter;
	}

	public FullDependencyTree build() {
		return new FullDependencyTree(buildRootNode());
	}

	private DependencyNode buildRootNode() {
		DependencyNode rootNode = new DependencyNode(mavenArtifactIdFrom(mavenProject), "nothing", new HashSet<SimpleArtifactId>());
		addDependentComponents(mavenProject, rootNode);
		return rootNode;
	}

	private void addDependentComponents(MavenProject aMavenProject, DependencyNode rootNode) {
		for (Artifact artifact : (Collection<Artifact>) aMavenProject.getDependencyArtifacts()) {
			addDependentComponent(rootNode, fullMavenArtifactIdFrom(artifact), artifact.getScope(), exclusions(aMavenProject, artifact));
		}
	}

	private Set<SimpleArtifactId> exclusions(MavenProject aMavenProject, Artifact artifact) {
		org.apache.maven.model.Dependency dependency = findDependency(aMavenProject, artifact);
		Set<SimpleArtifactId> result = new HashSet<SimpleArtifactId>();
		if (dependency != null) {
			for (org.apache.maven.model.Exclusion exclusion : (Collection<org.apache.maven.model.Exclusion>) dependency.getExclusions()) {
				result.add(new SimpleArtifactId(exclusion.getGroupId(), exclusion.getArtifactId()));
			}
		}
		return result;
	}

	private org.apache.maven.model.Dependency findDependency(MavenProject aMavenProject, Artifact artifact) {
		for (org.apache.maven.model.Dependency dependency : (Collection<org.apache.maven.model.Dependency>) aMavenProject
				.getDependencies()) {
			if (matches(artifact, dependency)) {
				return dependency;
			}
		}
		return null;
	}

	private boolean matches(Artifact artifact, org.apache.maven.model.Dependency dependency) {
		return mavenArtifactIdFrom(artifact).equals(mavenArtifactIdFrom(dependency));
	}

	private DependencyNode buildDependentComponent(DependencyNode parentNode, FullMavenArtifactId fullMavenArtifactId, String scope,
			Set<SimpleArtifactId> exclusions) {
		DependencyNode result = new DependencyNode(parentNode, fullMavenArtifactId, scope, exclusions);
		addDependentComponentsFor(result);
		return result;
	}

	private void addDependentComponentsFor(DependencyNode result) {
		if (result.getFullMavenArtifactId().heeftVersieRange()) {
			result.withDependentComponent(DependencyNode.emptyDependentComponent());
			return;
		}
		addDependentComponentsTo(result);
	}

	private void addDependentComponentsTo(DependencyNode result) {
		MavenProject mavenProject = findMavenProjectBy(result.getFullMavenArtifactId());
		if (mavenProject != null) {
			addDependentComponents(mavenProject, result);
		} else {
			Collection<Dependency> dependencies = dependencyRepository.getDependenciesFor(result.getFullMavenArtifactId());
			for (Dependency dependency : dependencies) {
				addDependentComponent(result, mavenArtifactIdFrom(dependency), dependency.getScope(), exclusions(dependency));
			}
		}
	}

	private Set<SimpleArtifactId> exclusions(Dependency dependency) {
		Set<SimpleArtifactId> result = new HashSet<SimpleArtifactId>();
		for (Exclusion exclusion : dependency.getExclusions()) {
			result.add(new SimpleArtifactId(exclusion.getGroupId(), exclusion.getArtifactId()));
		}
		return result;
	}

	private void addDependentComponent(DependencyNode result, FullMavenArtifactId mavenArtifactId, String scope,
			Set<SimpleArtifactId> exclusions) {
		if (artifactsFilter.apply(mavenArtifactId.getMavenArtifactId()) && !result.hasInPath(mavenArtifactId) && !"test".equals(scope)) {
			helper.getLog().debug("adding " + mavenArtifactId + " to " + result.getFullMavenArtifactId());
			showExclusions(mavenArtifactId, exclusions);
			result.withDependentComponent(buildDependentComponent(result, mavenArtifactId, scope, exclusions));
		}
	}

	private void showExclusions(FullMavenArtifactId fullMavenArtifactId, Set<SimpleArtifactId> exclusions) {
		if (!exclusions.isEmpty()) {
			helper.getLog().debug("excluding dependencies from " + fullMavenArtifactId);
			for (SimpleArtifactId excludedArtifactId : exclusions) {
				helper.getLog().debug("-" + excludedArtifactId);
			}
		}
	}

	private MavenProject findMavenProjectBy(FullMavenArtifactId fullMavenArtifactId) {
		for (MavenProject aMavenProject : (Collection<MavenProject>) mavenProject.getProjectReferences().values()) {
			if (fullMavenArtifactId.equals(mavenArtifactIdFrom(aMavenProject))) {
				return aMavenProject;
			}
		}
		return null;
	}

	private FullMavenArtifactId fullMavenArtifactIdFrom(Artifact artifact) {
		return new FullMavenArtifactId(mavenArtifactIdFrom(artifact), artifact.getVersion());
	}

	private MavenArtifactId mavenArtifactIdFrom(Artifact artifact) {
		return new MavenArtifactId(artifact.getGroupId(), artifact.getArtifactId(),
				artifact.getType());
	}

	private FullMavenArtifactId mavenArtifactIdFrom(MavenProject aMavenProject) {
		return new FullMavenArtifactId(new MavenArtifactId(aMavenProject.getGroupId(), aMavenProject.getArtifactId(),
				aMavenProject.getPackaging()), aMavenProject.getVersion());
	}

	private FullMavenArtifactId mavenArtifactIdFrom(Dependency dependency) {
		return new FullMavenArtifactId(new MavenArtifactId(dependency.getArtifact().getGroupId(), dependency.getArtifact().getArtifactId(),
				dependency.getArtifact().getExtension()), dependency.getArtifact().getVersion());
	}

	private MavenArtifactId mavenArtifactIdFrom(org.apache.maven.model.Dependency dependency) {
		return new MavenArtifactId(dependency.getGroupId(), dependency.getArtifactId(),
				dependency.getType());
	}

}
