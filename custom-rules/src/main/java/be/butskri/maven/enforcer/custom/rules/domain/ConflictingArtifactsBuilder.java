package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConflictingArtifactsBuilder {

	private FullDependencyTree fullTree;

	public ConflictingArtifactsBuilder(FullDependencyTree fullTree) {
		this.fullTree = fullTree;
	}

	public Collection<ConflictingArtifact> build() {
		Map<MavenArtifactId, ConflictingArtifact> conflictingArtifacts = new HashMap<MavenArtifactId, ConflictingArtifact>();
		for (DependencyNode dependencyNode : fullTree.getAllDependentNodes()) {
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
