package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

public class MavenProjectRepository {

	private MavenProject rootMavenProject;
	private ArtifactFactory artifactFactory;
	private ArtifactRepository localRepository;
	private MavenProjectBuilder mavenProjectBuilder;

	public MavenProjectRepository(EnforcerRuleHelper enforcerRuleHelper) {
		this.rootMavenProject = EnforcerRuleUtils.mavenProjectFrom(enforcerRuleHelper);
		this.artifactFactory = EnforcerRuleUtils.artifactFactoryFrom(enforcerRuleHelper);
		this.localRepository = EnforcerRuleUtils.localRepositoryFrom(enforcerRuleHelper);
		this.mavenProjectBuilder = EnforcerRuleUtils.mavenProjectBuilderFrom(enforcerRuleHelper);
	}

	public MavenProject findMavenProject(FullMavenArtifactId fullMavenArtifactId) {
		MavenProject foundMavenProject = findMavenProjectInReactor(fullMavenArtifactId);
		if (foundMavenProject == null) {
			foundMavenProject = findReleasedMavenProject(fullMavenArtifactId);
		}
		return foundMavenProject;
	}

	private MavenProject findMavenProjectInReactor(FullMavenArtifactId fullMavenArtifactId) {
		if (matches(rootMavenProject, fullMavenArtifactId)) {
			return rootMavenProject;
		}
		for (MavenProject aMavenProject : (Collection<MavenProject>) rootMavenProject.getProjectReferences().values()) {
			if (matches(aMavenProject, fullMavenArtifactId)) {
				return aMavenProject;
			}
		}
		return null;
	}

	private boolean matches(MavenProject aMavenProject, FullMavenArtifactId fullMavenArtifactId) {
		return fullMavenArtifactId.equals(FullMavenArtifactId.fullMavenArtifactIdFrom(aMavenProject));
	}

	private MavenProject findReleasedMavenProject(FullMavenArtifactId fullMavenArtifactId) {
		try {
			return mavenProjectBuilder.buildFromRepository(toArtifact(fullMavenArtifactId),
					rootMavenProject.getRemoteArtifactRepositories(),
					localRepository);
		} catch (ProjectBuildingException e) {
			throw new RuntimeException(e);
		}
	}

	private Artifact toArtifact(FullMavenArtifactId fullMavenArtifactId) {
		MavenArtifactId mavenArtifactId = fullMavenArtifactId.getMavenArtifactId();
		return artifactFactory.createArtifact(mavenArtifactId.getGroupId(), mavenArtifactId.getArtifactId(),
				fullMavenArtifactId.getVersion(), "compile", mavenArtifactId.getType());
	}
}
