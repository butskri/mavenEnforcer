package be.butskri.maven.enforcer.custom.rules.domain;

import static be.butskri.maven.enforcer.custom.rules.domain.FullMavenArtifactId.fullMavenArtifactIdFrom;
import static be.butskri.maven.enforcer.custom.rules.domain.MavenArtifactId.mavenArtifactIdFrom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class MavenProjectDependencyNodeRepository implements DependencyNodeRepository {

	private MavenProject mavenProject;

	public MavenProjectDependencyNodeRepository(MavenProject mavenProject) {
		this.mavenProject = mavenProject;
	}

	public List<DependencyNode> findDependentComponentsFor(DependencyNode parentNode) {
		MavenProject foundMavenProject = findMavenProjectBy(parentNode.getFullMavenArtifactId());
		if (foundMavenProject == null) {
			return null;
		}
		return dependencyNodesFor(foundMavenProject);
	}

	private MavenProject findMavenProjectBy(FullMavenArtifactId fullMavenArtifactId) {
		if (matches(mavenProject, fullMavenArtifactId)) {
			return mavenProject;
		}
		for (MavenProject aMavenProject : (Collection<MavenProject>) mavenProject.getProjectReferences().values()) {
			if (matches(aMavenProject, fullMavenArtifactId)) {
				return aMavenProject;
			}
		}
		return null;
	}

	private boolean matches(MavenProject aMavenProject, FullMavenArtifactId fullMavenArtifactId) {
		return fullMavenArtifactId.equals(FullMavenArtifactId.fullMavenArtifactIdFrom(aMavenProject));
	}

	private List<DependencyNode> dependencyNodesFor(MavenProject foundMavenProject) {
		List<DependencyNode> result = new ArrayList<DependencyNode>();
		for (Artifact artifact : (Collection<Artifact>) foundMavenProject.getDependencyArtifacts()) {
			result.add(new DependencyNode(fullMavenArtifactIdFrom(artifact), artifact.getScope(), exclusions(foundMavenProject, artifact)));
		}
		return result;
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
