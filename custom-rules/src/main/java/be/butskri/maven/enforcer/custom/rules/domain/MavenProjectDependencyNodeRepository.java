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
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class MavenProjectDependencyNodeRepository implements DependencyNodeRepository {

	private MavenProject mavenProject;
	private EnforcerRuleHelper enforcerRuleHelper;
	private ArtifactFactory artifactFactory;

	public MavenProjectDependencyNodeRepository(EnforcerRuleHelper enforcerRuleHelper, MavenProject mavenProject) {
		this.enforcerRuleHelper = enforcerRuleHelper;
		this.mavenProject = mavenProject;
		try {
			this.artifactFactory = (ArtifactFactory) enforcerRuleHelper.getComponent(ArtifactFactory.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException(e);
		}
	}

	public List<DependencyNode> findDependentComponentsFor(DependencyNode parentNode) {
		MavenProject foundMavenProject = findMavenProjectBy(parentNode.getFullMavenArtifactId());
		if (foundMavenProject == null) {
			foundMavenProject = findReleasedMavenProjectBy(parentNode);
		}
		return dependencyNodesFor(foundMavenProject);
	}

	private MavenProject findReleasedMavenProjectBy(DependencyNode parentNode) {
		try {
			MavenSession session = (MavenSession) enforcerRuleHelper.evaluate("${session}");
			MavenProjectBuilder mavenProjectBuilder = (MavenProjectBuilder) enforcerRuleHelper.getComponent(MavenProjectBuilder.class);
			ArtifactRepository localRepository = session.getLocalRepository();
			return mavenProjectBuilder.buildFromRepository(toArtifact(parentNode),
					mavenProject.getRemoteArtifactRepositories(),
					localRepository);
		} catch (ProjectBuildingException e) {
			throw new RuntimeException(e);
		} catch (ExpressionEvaluationException e) {
			throw new RuntimeException(e);
		} catch (ComponentLookupException e) {
			throw new RuntimeException(e);
		}
	}

	private Artifact toArtifact(DependencyNode parentNode) {
		FullMavenArtifactId fullMavenArtifactId = parentNode.getFullMavenArtifactId();
		MavenArtifactId mavenArtifactId = fullMavenArtifactId.getMavenArtifactId();
		return artifactFactory.createArtifact(mavenArtifactId.getGroupId(), mavenArtifactId.getArtifactId(),
				fullMavenArtifactId.getVersion(), parentNode.getScope(), mavenArtifactId.getType());
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
