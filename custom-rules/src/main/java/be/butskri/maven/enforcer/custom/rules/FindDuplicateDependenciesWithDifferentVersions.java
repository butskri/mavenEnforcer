package be.butskri.maven.enforcer.custom.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import be.butskri.maven.enforcer.custom.rules.appender.LogLevel;
import be.butskri.maven.enforcer.custom.rules.domain.ConflictingArtifact;
import be.butskri.maven.enforcer.custom.rules.domain.ConflictingArtifactsBuilder;
import be.butskri.maven.enforcer.custom.rules.domain.DependencyNode;
import be.butskri.maven.enforcer.custom.rules.domain.DependencyNodeByArtifactRegexFilter;
import be.butskri.maven.enforcer.custom.rules.domain.FullDependencyTree;
import be.butskri.maven.enforcer.custom.rules.domain.FullDependencyTreeBuilder;

import com.google.common.base.Predicate;

public class FindDuplicateDependenciesWithDifferentVersions implements EnforcerRule {

	private Set<String> dependenciesToBeChecked = new HashSet<String>();
	private DependencyNodeByArtifactRegexFilter dependencyNodeFilter;

	public void setDependenciesToBeChecked(Set<String> dependenciesToBeChecked) {
		this.dependenciesToBeChecked = dependenciesToBeChecked;
	}

	public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
		try {
			MavenProject project = (MavenProject) helper.evaluate("${project}");
			FullDependencyTree tree = buildFullDependencyTree(helper, project);

			Collection<ConflictingArtifact> conflictingArtifacts = findConflictingArtifacts(tree);
			if (!conflictingArtifacts.isEmpty()) {
				throw new EnforcerRuleException(buildMessage(project, conflictingArtifacts));
			}
		} catch (ExpressionEvaluationException e) {
			helper.getLog().error(String.format("problem evaluating expression ${project}"), e);
		}
	}

	private FullDependencyTree buildFullDependencyTree(EnforcerRuleHelper helper, MavenProject project) {
		Log log = helper.getLog();
		log.debug("builden van tree...");
		FullDependencyTree tree = new FullDependencyTreeBuilder(project, helper).build();
		log.debug("tree gebuild.");
		log.debug("printen tree ...");
		tree.print(log, LogLevel.DEBUG);
		log.debug("tree geprint.");
		return tree;
	}

	private String buildMessage(MavenProject project, Collection<ConflictingArtifact> conflictingArtifacts) {
		StringBuilder result = new StringBuilder();
		result.append("enforcer rule failed because of found dependencies with different versions.\n");

		for (ConflictingArtifact conflictingArtifact : conflictingArtifacts) {
			result.append(conflictingArtifact.getMessage());
		}
		return result.toString();
	}

	private Collection<ConflictingArtifact> findConflictingArtifacts(FullDependencyTree tree) {
		return new ConflictingArtifactsBuilder(tree, dependencyNodeFilter()).build();
	}

	private Predicate<DependencyNode> dependencyNodeFilter() {
		if (dependencyNodeFilter == null) {
			dependencyNodeFilter = new DependencyNodeByArtifactRegexFilter(dependenciesToBeChecked);
		}
		return dependencyNodeFilter;
	}

	public boolean isCacheable() {
		return false;
	}

	public boolean isResultValid(EnforcerRule cachedRule) {
		return false;
	}

	public String getCacheId() {
		return "";
	}

}
