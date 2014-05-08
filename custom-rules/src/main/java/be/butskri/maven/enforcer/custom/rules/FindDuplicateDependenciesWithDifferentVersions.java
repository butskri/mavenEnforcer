package be.butskri.maven.enforcer.custom.rules;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import be.butskri.maven.enforcer.custom.rules.appender.LogLevel;
import be.butskri.maven.enforcer.custom.rules.appender.LoggingStringAppender;
import be.butskri.maven.enforcer.custom.rules.domain.CompositeDependencyNodeRepository;
import be.butskri.maven.enforcer.custom.rules.domain.ConflictingArtifact;
import be.butskri.maven.enforcer.custom.rules.domain.ConflictingArtifactsBuilder;
import be.butskri.maven.enforcer.custom.rules.domain.DependencyNode;
import be.butskri.maven.enforcer.custom.rules.domain.DependencyNodeByArtifactRegexFilter;
import be.butskri.maven.enforcer.custom.rules.domain.DependencyNodeRepository;
import be.butskri.maven.enforcer.custom.rules.domain.EnforcerRuleUtils;
import be.butskri.maven.enforcer.custom.rules.domain.FullDependencyTree;
import be.butskri.maven.enforcer.custom.rules.domain.FullDependencyTreeBuilder;
import be.butskri.maven.enforcer.custom.rules.domain.MavenArtifactIdRegexFilter;
import be.butskri.maven.enforcer.custom.rules.domain.MavenProjectDependencyNodeRepository;

import com.google.common.base.Predicate;

public class FindDuplicateDependenciesWithDifferentVersions implements EnforcerRule {

	private boolean showTree = false;
	private Set<String> includePathsToBeChecked;
	private Set<String> excludePathsToBeChecked;
	private Set<String> includeDependenciesToBeChecked;
	private Set<String> excludeDependenciesToBeChecked;
	private MavenArtifactIdRegexFilter pathsToBeCheckedFilter;
	private DependencyNodeByArtifactRegexFilter dependenciesToBeCheckedFilter;

	public void setShowTree(boolean showTree) {
		this.showTree = showTree;
	}

	public void setIncludeDependenciesToBeChecked(Set<String> includeDependenciesToBeChecked) {
		this.includeDependenciesToBeChecked = includeDependenciesToBeChecked;
	}

	public void setExcludeDependenciesToBeChecked(Set<String> excludeDependenciesToBeChecked) {
		this.excludeDependenciesToBeChecked = excludeDependenciesToBeChecked;
	}

	public void setIncludePathsToBeChecked(Set<String> includePathsToBeChecked) {
		this.includePathsToBeChecked = includePathsToBeChecked;
	}

	public void setExcludePathsToBeChecked(Set<String> excludePathsToBeChecked) {
		this.excludePathsToBeChecked = excludePathsToBeChecked;
	}

	public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
		MavenProject project = EnforcerRuleUtils.mavenProjectFrom(helper);
		FullDependencyTree tree = buildFullDependencyTree(helper, project);

		Collection<ConflictingArtifact> conflictingArtifacts = findConflictingArtifacts(project, tree);
		if (!conflictingArtifacts.isEmpty()) {
			throw new EnforcerRuleException(buildMessage(project, conflictingArtifacts));
		}
	}

	private FullDependencyTree buildFullDependencyTree(EnforcerRuleHelper helper, MavenProject project) {
		Log log = helper.getLog();
		log.debug("building tree...");
		DependencyNodeRepository dependencyNodeRepository = buildDependencyNodeRepository(helper);
		FullDependencyTree tree = new FullDependencyTreeBuilder(project, dependencyNodeRepository, pathsToBeCheckedFilter()).build();
		log.debug("tree was built.");
		printTree(log, tree);
		return tree;
	}

	private DependencyNodeRepository buildDependencyNodeRepository(EnforcerRuleHelper helper) {
		return new CompositeDependencyNodeRepository(new MavenProjectDependencyNodeRepository(helper));
	}

	private void printTree(Log log, FullDependencyTree tree) {
		LogLevel logLevel = determineLogLevelForPrintingTree();
		LoggingStringAppender loggingStringAppender = new LoggingStringAppender(log, logLevel);
		loggingStringAppender.append("--found tree:");
		tree.print(log, logLevel);
		loggingStringAppender.append("--end of tree");
	}

	private LogLevel determineLogLevelForPrintingTree() {
		if (showTree) {
			return LogLevel.INFO;
		}
		return LogLevel.DEBUG;
	}

	private String buildMessage(MavenProject project, Collection<ConflictingArtifact> conflictingArtifacts) {
		StringBuilder result = new StringBuilder();
		result.append("enforcer rule failed because of found dependencies with different versions.\n");

		for (ConflictingArtifact conflictingArtifact : conflictingArtifacts) {
			result.append(conflictingArtifact.getMessage());
		}
		return result.toString();
	}

	private Collection<ConflictingArtifact> findConflictingArtifacts(MavenProject project, FullDependencyTree tree) {
		return new ConflictingArtifactsBuilder(project, tree, dependenciesToBeCheckedFilter()).build();
	}

	private Predicate<DependencyNode> dependenciesToBeCheckedFilter() {
		if (dependenciesToBeCheckedFilter == null) {
			dependenciesToBeCheckedFilter = DependencyNodeByArtifactRegexFilter.dependencyNodeByArtifactRegexFilter(
					includeDependenciesToBeChecked, excludeDependenciesToBeChecked);
		}
		return dependenciesToBeCheckedFilter;
	}

	private MavenArtifactIdRegexFilter pathsToBeCheckedFilter() {
		if (pathsToBeCheckedFilter == null) {
			pathsToBeCheckedFilter = new MavenArtifactIdRegexFilter(includePathsToBeChecked, excludePathsToBeChecked);
		}
		return pathsToBeCheckedFilter;
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
