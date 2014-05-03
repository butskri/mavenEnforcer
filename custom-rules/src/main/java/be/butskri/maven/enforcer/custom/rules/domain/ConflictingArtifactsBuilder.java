package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ConflictingArtifactsBuilder {

	private MavenProject mavenProject;
	private FullDependencyTree fullTree;
	private Predicate<DependencyNode> dependencyNodeFilter;
	private Map<MavenArtifactId, ConflictingArtifact> conflictingArtifacts;

	public ConflictingArtifactsBuilder(MavenProject mavenProject, FullDependencyTree fullTree,
			Predicate<DependencyNode> dependencyNodeFilter) {
		this.mavenProject = mavenProject;
		this.fullTree = fullTree;
		this.dependencyNodeFilter = dependencyNodeFilter;
	}

	public Collection<ConflictingArtifact> build() {
		conflictingArtifacts = new HashMap<MavenArtifactId, ConflictingArtifact>();
		addDependencyNodesFromFullTreeToConflictingArtifactsMap();
		addDependencyNodesFromDependencyManagementToConflictingArtifactsMap();

		return alleenArtifactsMetConflicten();
	}

	private void addDependencyNodesFromDependencyManagementToConflictingArtifactsMap() {
		for (Dependency dependency : (Collection<Dependency>) mavenProject.getDependencyManagement().getDependencies()) {
			addDependencyNodeToConflictingArtifacts(dependencyMgtDependencyToDependencyNode(dependency));
		}
	}

	private DependencyNode dependencyMgtDependencyToDependencyNode(Dependency dependency) {
		DependencyNode rootNode = new DependencyNode(FullMavenArtifactId.fullMavenArtifactIdFrom(mavenProject), "noScope",
				new HashSet<SimpleArtifactId>());
		DependencyNode node = new DependencyNode(rootNode, new FullMavenArtifactId(new MavenArtifactId(dependency.getGroupId(),
				dependency.getArtifactId(),
				dependency.getType()), dependency.getVersion()), "depMgt", new HashSet<SimpleArtifactId>());
		rootNode.withDependentComponent(node);
		return node;
	}

	private void addDependencyNodesFromFullTreeToConflictingArtifactsMap() {
		for (DependencyNode dependencyNode : Collections2.filter(fullTree.getAllDependentNodes(), dependencyNodeFilter)) {
			addDependencyNodeToConflictingArtifacts(dependencyNode);
		}
	}

	private void addDependencyNodeToConflictingArtifacts(DependencyNode dependencyNode) {
		MavenArtifactId mavenArtifactId = dependencyNode.getFullMavenArtifactId().getMavenArtifactId();
		ConflictingArtifact conflictingArtifact = conflictingArtifacts.get(mavenArtifactId);
		if (conflictingArtifact == null) {
			conflictingArtifacts.put(mavenArtifactId, new ConflictingArtifact(dependencyNode));
		} else {
			conflictingArtifact.addNode(dependencyNode);
		}
	}

	private Collection<ConflictingArtifact> alleenArtifactsMetConflicten() {
		Collection<ConflictingArtifact> values = conflictingArtifacts.values();
		Collection<ConflictingArtifact> result = new ArrayList<ConflictingArtifact>();
		for (ConflictingArtifact conflictingArtifact : values) {
			if (conflictingArtifact.containsConflicts()) {
				result.add(conflictingArtifact);
			}
		}
		return result;
	}

}
