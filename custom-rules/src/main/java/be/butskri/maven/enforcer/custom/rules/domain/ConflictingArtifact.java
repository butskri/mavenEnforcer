package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class ConflictingArtifact {

	private MavenArtifactId mavenArtifactId;
	private Collection<DependencyNode> conflictingNodes = new ArrayList<DependencyNode>();

	public ConflictingArtifact(DependencyNode dependencyNode) {
		this.mavenArtifactId = dependencyNode.getFullMavenArtifactId().getMavenArtifactId();
		this.conflictingNodes.add(dependencyNode);
	}

	void addNode(DependencyNode dependencyNode) {
		this.conflictingNodes.add(dependencyNode);
	}

	public MavenArtifactId getMavenArtifactId() {
		return mavenArtifactId;
	}

	public Collection<DependencyNode> getConflictingNodes() {
		return conflictingNodes;
	}

	public boolean containsConflicts() {
		return getAllVersions().size() > 1;
	}

	public Set<String> getAllVersions() {
		Set<String> result = new HashSet<String>();
		for (DependencyNode dependencyNode : conflictingNodes) {
			result.add(dependencyNode.getFullMavenArtifactId().getVersion());
		}
		return result;
	}

	public String getMessage() {
		StringBuilder result = new StringBuilder();
		result.append(showFoundVersionsMessage());
		result.append("\n");
		for (DependencyNode dependencyNode : conflictingNodes) {
			result.append(dependencyNode.getPath());
		}
		return result.toString();
	}

	private String showFoundVersionsMessage() {
		return String.format("found versions for component %s: %s", getMavenArtifactId(), StringUtils.join(getAllVersions(), ","));
	}

}
