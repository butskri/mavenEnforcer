package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.Collection;
import java.util.List;

import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;

import be.butskri.maven.enforcer.custom.rules.aether.RemoteRepositoryFactory;

public class DependencyRepository {

	private RepositorySystem system;
	private RepositorySystemSession session;
	private Log log;

	public static DependencyRepository getRepository(EnforcerRuleHelper helper) {
		try {
			RepositorySystem system = (RepositorySystem) helper.getComponent(RepositorySystem.class);
			RepositorySystemSession session = (RepositorySystemSession) helper.evaluate("${repositorySystemSession}");
			return new DependencyRepository(system, session, helper.getLog());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public DependencyRepository(RepositorySystem system, RepositorySystemSession session, Log log) {
		this.system = system;
		this.session = session;
		this.log = log;
	}

	public Collection<Dependency> getDependenciesFor(FullMavenArtifactId fullMavenArtifactId) {
		return getDependenciesFor(toArtifact(fullMavenArtifactId));
	}

	public Collection<Dependency> getDependenciesFor(Artifact artifact) {
		ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
		descriptorRequest.setArtifact(artifact);
		descriptorRequest.setRepositories(RemoteRepositoryFactory.getRepositories());

		try {
			ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);
			return logAndReturn(artifact, descriptorResult);
		} catch (ArtifactDescriptorException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Dependency> logAndReturn(Artifact artifact, ArtifactDescriptorResult descriptorResult) {
		log.debug("found dependencies for " + artifact + ":");
		List<Dependency> result = descriptorResult.getDependencies();
		for (Dependency dependency : result) {
			log.debug(" " + dependency);
		}
		return result;
	}

	private Artifact toArtifact(FullMavenArtifactId fullMavenArtifactId) {
		MavenArtifactId mavenArtifactId = fullMavenArtifactId.getMavenArtifactId();
		Artifact artifact = new DefaultArtifact(mavenArtifactId.getGroupId(), mavenArtifactId.getArtifactId(), mavenArtifactId.getType(),
				fullMavenArtifactId.getVersion());
		return artifact;
	}

}
