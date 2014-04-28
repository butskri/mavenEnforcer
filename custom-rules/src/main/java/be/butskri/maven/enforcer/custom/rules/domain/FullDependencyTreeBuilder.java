package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.Collection;
import java.util.HashSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.graph.Dependency;

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
		DependencyNode rootNode = new DependencyNode(mavenArtifactIdFrom(mavenProject), "nothing", new HashSet<MavenArtifactId>());
		addDependentComponents(mavenProject, rootNode);
		return rootNode;
	}

	private void addDependentComponents(MavenProject aMavenProject, DependencyNode rootNode) {
		for (Artifact artifact : (Collection<Artifact>) aMavenProject.getDependencyArtifacts()) {
			addDependentComponent(rootNode, mavenArtifactIdFrom(artifact), artifact.getScope());
		}
	}

	private DependencyNode buildDependentComponent(DependencyNode parentNode, FullMavenArtifactId fullMavenArtifactId, String scope) {
		DependencyNode result = new DependencyNode(parentNode, fullMavenArtifactId, scope, new HashSet<MavenArtifactId>());
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
				addDependentComponent(result, mavenArtifactIdFrom(dependency), dependency.getScope());
			}
		}
	}

	private void addDependentComponent(DependencyNode result, FullMavenArtifactId mavenArtifactId, String scope) {
		if (artifactsFilter.apply(mavenArtifactId.getMavenArtifactId()) && !result.heeftInPath(mavenArtifactId) && !"test".equals(scope)) {
			helper.getLog().debug("adding " + mavenArtifactId + " to " + result.getFullMavenArtifactId());
			result.withDependentComponent(buildDependentComponent(result, mavenArtifactId, scope));
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

	private FullMavenArtifactId mavenArtifactIdFrom(Artifact artifact) {
		return new FullMavenArtifactId(new MavenArtifactId(artifact.getGroupId(), artifact.getArtifactId(),
				artifact.getType()), artifact.getVersion());
	}

	private FullMavenArtifactId mavenArtifactIdFrom(MavenProject aMavenProject) {
		return new FullMavenArtifactId(new MavenArtifactId(aMavenProject.getGroupId(), aMavenProject.getArtifactId(),
				aMavenProject.getPackaging()), aMavenProject.getVersion());
	}

	private FullMavenArtifactId mavenArtifactIdFrom(Dependency dependency) {
		return new FullMavenArtifactId(new MavenArtifactId(dependency.getArtifact().getGroupId(), dependency.getArtifact().getArtifactId(),
				dependency.getArtifact().getExtension()), dependency.getArtifact().getVersion());
	}

}
