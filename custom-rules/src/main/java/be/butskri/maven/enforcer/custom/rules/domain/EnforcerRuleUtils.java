package be.butskri.maven.enforcer.custom.rules.domain;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class EnforcerRuleUtils {

	public static ArtifactFactory artifactFactoryFrom(EnforcerRuleHelper enforcerRuleHelper) {
		try {
			return (ArtifactFactory) enforcerRuleHelper.getComponent(ArtifactFactory.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException(e);
		}
	}

	public static MavenProject mavenProjectFrom(EnforcerRuleHelper enforcerRuleHelper) {
		try {
			return (MavenProject) enforcerRuleHelper.evaluate("${project}");
		} catch (ExpressionEvaluationException e) {
			enforcerRuleHelper.getLog().error(String.format("problem evaluating expression ${project}"), e);
			throw new RuntimeException(e);
		}
	}

	public static MavenProjectBuilder mavenProjectBuilderFrom(EnforcerRuleHelper enforcerRuleHelper) {
		try {
			return (MavenProjectBuilder) enforcerRuleHelper.getComponent(MavenProjectBuilder.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException(e);
		}
	}

	public static ArtifactRepository localRepositoryFrom(EnforcerRuleHelper enforcerRuleHelper) {
		try {
			MavenSession session = (MavenSession) enforcerRuleHelper.evaluate("${session}");
			return session.getLocalRepository();
		} catch (ExpressionEvaluationException e) {
			throw new RuntimeException(e);
		}
	}

}
