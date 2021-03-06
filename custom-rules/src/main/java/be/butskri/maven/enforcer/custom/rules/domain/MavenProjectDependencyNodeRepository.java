package be.butskri.maven.enforcer.custom.rules.domain;

import static be.butskri.maven.enforcer.custom.rules.domain.FullMavenArtifactId.fullMavenArtifactIdFrom;
import static be.butskri.maven.enforcer.custom.rules.domain.MavenArtifactId.mavenArtifactIdFrom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;

public class MavenProjectDependencyNodeRepository implements DependencyNodeRepository {

	private ArtifactFactory artifactFactory;
	private MavenProjectRepository mavenProjectRepository;

	public MavenProjectDependencyNodeRepository(EnforcerRuleHelper enforcerRuleHelper) {
		this.artifactFactory = EnforcerRuleUtils.artifactFactoryFrom(enforcerRuleHelper);
		this.mavenProjectRepository = new MavenProjectRepository(enforcerRuleHelper);
	}

	public List<DependencyNode> findDependentComponentsFor(DependencyNode dependencyNode) {
		MavenProject foundMavenProject = mavenProjectRepository.findMavenProject(dependencyNode.getFullMavenArtifactId());
		return dependencyNodesFor(foundMavenProject);
	}

	private List<DependencyNode> dependencyNodesFor(MavenProject foundMavenProject) {
		List<DependencyNode> result = new ArrayList<DependencyNode>();
		for (Artifact artifact : getDependencyArtifacts(foundMavenProject)) {
			result.add(new DependencyNode(fullMavenArtifactIdFrom(artifact), artifact.getScope(), exclusions(foundMavenProject, artifact)));
		}
		return result;
	}

	private Set<Artifact> getDependencyArtifacts(MavenProject foundMavenProject) {
		try {
			Set<Artifact> dependencyArtifacts = foundMavenProject.getDependencyArtifacts();
			if (dependencyArtifacts == null) {
				dependencyArtifacts = foundMavenProject.createArtifacts(artifactFactory, null, null);
			}
			return dependencyArtifacts;
		} catch (InvalidDependencyVersionException e) {
			throw new RuntimeException(e);
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

}
