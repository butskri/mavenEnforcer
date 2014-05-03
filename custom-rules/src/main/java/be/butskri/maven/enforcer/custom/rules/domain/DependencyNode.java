package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import be.butskri.maven.enforcer.custom.rules.appender.CollectingStringAppender;
import be.butskri.maven.enforcer.custom.rules.appender.StringAppender;

import com.google.common.collect.Sets;

public class DependencyNode {

	private DependencyNode parentNode;
	private FullMavenArtifactId fullMavenArtifactId;
	private String scope;
	private Set<SimpleArtifactId> exclusions;
	private List<DependencyNode> dependentComponents = new ArrayList<DependencyNode>();

	public static DependencyNode emptyDependentComponent() {
		return new DependencyNode(new FullMavenArtifactId(new MavenArtifactId("??", "??", "??"), "??"),
				Sets.<SimpleArtifactId> newHashSet());
	}

	public DependencyNode(FullMavenArtifactId fullMavenArtifactId, Set<SimpleArtifactId> exclusions) {
		this.fullMavenArtifactId = fullMavenArtifactId;
		this.exclusions = exclusions;
	}

	public DependencyNode(FullMavenArtifactId fullMavenArtifactId, String scope, Set<SimpleArtifactId> exclusions) {
		this(null, fullMavenArtifactId, scope, exclusions);
	}

	public DependencyNode(DependencyNode parentNode, FullMavenArtifactId fullMavenArtifactId, String scope, Set<SimpleArtifactId> exclusions) {
		this.parentNode = parentNode;
		this.fullMavenArtifactId = fullMavenArtifactId;
		this.scope = scope;
		this.exclusions = exclusions;
	}

	public FullMavenArtifactId getFullMavenArtifactId() {
		return fullMavenArtifactId;
	}

	public String getScope() {
		return scope;
	}

	public Set<SimpleArtifactId> getExclusions() {
		return exclusions;
	}

	public List<DependencyNode> getDependentComponents() {
		return dependentComponents;
	}

	DependencyNode withDependentComponent(DependencyNode dependentComponent) {
		dependentComponent.parentNode = this;
		dependentComponents.add(dependentComponent);
		return this;
	}

	public void print(StringAppender stringAppender, boolean... lastElements) {
		stringAppender.append(treeLine(lastElements));
		int lastIndex = getDependentComponents().size() - 1;
		for (int index = 0; index < getDependentComponents().size(); index++) {
			getDependentComponents().get(index).print(stringAppender, lastElements(lastElements, index == lastIndex));
		}
	}

	private boolean[] lastElements(boolean[] lastElements, boolean additionalElement) {
		boolean[] result = new boolean[lastElements.length + 1];
		for (int i = 0; i < lastElements.length; i++) {
			result[i] = lastElements[i];
		}
		result[lastElements.length] = additionalElement;
		return result;
	}

	private String treeLine(boolean... lastElements) {
		if (lastElements.length == 0) {
			return getFullMavenArtifactId().toString();
		} else {
			StringBuilder result = new StringBuilder();
			result.append(prefixes(lastElements));
			result.append(getFullMavenArtifactId().toString());
			result.append(":");
			result.append(scope);
			return result.toString();
		}
	}

	private String prefixes(boolean... lastElements) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < lastElements.length - 1; i++) {
			if (lastElements[i]) {
				result.append("   ");
			} else {
				result.append("|  ");
			}
		}
		if (lastElements[lastElements.length - 1]) {
			result.append("\\- ");
		} else {
			result.append("+- ");
		}

		return result.toString();
	}

	public boolean hasInPath(FullMavenArtifactId mavenArtifactId) {
		if (this.fullMavenArtifactId.equals(mavenArtifactId)) {
			return true;
		}
		if (parentNode == null) {
			return false;
		}
		return parentNode.hasInPath(mavenArtifactId);
	}

	public Collection<DependencyNode> getAllDependentNodes() {
		Set<DependencyNode> result = new HashSet<DependencyNode>();
		for (DependencyNode dependencyNode : dependentComponents) {
			if (!dependencyNode.isExcluded()) {
				result.add(dependencyNode);
				result.addAll(dependencyNode.getAllDependentNodes());
			}
		}

		return result;
	}

	boolean isExcluded() {
		if (parentNode == null) {
			return false;
		}
		if (parentNode.isExcluded()) {
			return true;
		}
		return getAllExclusions().contains(getSimpleArtifactId());
	}

	private Set<SimpleArtifactId> getAllExclusions() {
		Set<SimpleArtifactId> result = new HashSet<SimpleArtifactId>();
		if (parentNode != null) {
			result.addAll(parentNode.getExclusions());
			result.addAll(parentNode.getAllExclusions());
		}
		return result;
	}

	private SimpleArtifactId getSimpleArtifactId() {
		return getFullMavenArtifactId().getMavenArtifactId().getSimpleArtifactId();
	}

	public String getPath() {
		CollectingStringAppender collectingStringAppender = new CollectingStringAppender();
		DependencyNode path = buildPathAsNode();
		path.print(collectingStringAppender);
		return StringUtils.join(collectingStringAppender.getCollectedStrings(), "\n") + "\n";
	}

	private DependencyNode buildPathAsNode() {
		DependencyNode result = copy(this);
		DependencyNode currentNode = this.parentNode;
		while (currentNode != null) {
			DependencyNode parentNode = copy(currentNode);
			parentNode.withDependentComponent(result);
			result = parentNode;
			currentNode = currentNode.parentNode;
		}
		return result;
	}

	private DependencyNode copy(DependencyNode dependencyNode) {
		return new DependencyNode(dependencyNode.getFullMavenArtifactId(), dependencyNode.getScope(), new HashSet<SimpleArtifactId>());
	}

	@Override
	public String toString() {
		if (parentNode == null) {
			return this.fullMavenArtifactId.toString();
		}
		return String.format("%s > %s", parentNode.toString(), fullMavenArtifactId.toString());
	}

	public boolean isRecursive() {
		if (parentNode == null) {
			return false;
		}
		return parentNode.hasInPath(getFullMavenArtifactId());
	}

	DependencyNode getParentNode() {
		return parentNode;
	}
}
