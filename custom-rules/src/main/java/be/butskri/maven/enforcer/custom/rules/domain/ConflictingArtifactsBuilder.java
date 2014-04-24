package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ConflictingArtifactsBuilder {

	private FullDependencyTree fullTree;
	private Predicate<DependencyNode> dependencyNodeFilter;

	public ConflictingArtifactsBuilder(FullDependencyTree fullTree, Predicate<DependencyNode> dependencyNodeFilter) {
		this.fullTree = fullTree;
		this.dependencyNodeFilter = dependencyNodeFilter;
	}

	public Collection<ConflictingArtifact> build() {
		Map<MavenArtifactId, ConflictingArtifact> conflictingArtifacts = new HashMap<MavenArtifactId, ConflictingArtifact>();
		for (DependencyNode dependencyNode : Collections2.filter(fullTree.getAllDependentNodes(), dependencyNodeFilter)) {
			MavenArtifactId mavenArtifactId = dependencyNode.getFullMavenArtifactId().getMavenArtifactId();
			ConflictingArtifact conflictingArtifact = conflictingArtifacts.get(mavenArtifactId);
			if (conflictingArtifact == null) {
				conflictingArtifacts.put(mavenArtifactId, new ConflictingArtifact(dependencyNode));
			} else {
				conflictingArtifact.addNode(dependencyNode);
			}
		}

		return alleenArtifactsMetConflicten(conflictingArtifacts.values());
	}

	private Collection<ConflictingArtifact> alleenArtifactsMetConflicten(Collection<ConflictingArtifact> values) {
		Collection<ConflictingArtifact> result = new ArrayList<ConflictingArtifact>();
		for (ConflictingArtifact conflictingArtifact : values) {
			if (conflictingArtifact.containsConflicts()) {
				result.add(conflictingArtifact);
			}
		}
		return result;
	}

}
