package be.butskri.maven.enforcer.custom.rules.domain;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Predicate;

public class ConflictingArtifactsBuilderTest {

	@Test
	public void canBuildConflictingArtifacts() {
		DependencyNode conflictingNode11 = dependencyNode("conflictingGroupId:conflictingArtifactId:jar:4.0.0", "compile");
		DependencyNode conflictingNode12 = dependencyNode("conflictingGroupId:conflictingArtifactId:jar:4.1.0", "compile");

		DependencyNode conflictingNode21 = dependencyNode("anotherConflictingGroupId:anotherConflictingArtifactId:jar:1.2.3", "compile");
		DependencyNode conflictingNode22 = dependencyNode("anotherConflictingGroupId:anotherConflictingArtifactId:jar:7.8.9", "compile");
		DependencyNode conflictingNode23 = dependencyNode("anotherConflictingGroupId:anotherConflictingArtifactId:jar:3.4.5", "runtime");

		DependencyNode dependentComponent1 = dependencyNode("childGroupId:childArtifactId:jar:1.0.0", "compile")
				.withDependentComponent(dependencyNode("grandChildGroupId:grandChildArtifactId:jar:2.0.0", "compile"))
				.withDependentComponent(dependencyNode("anothergrandChildGroupId:anotherGrandChildArtifactId:jar:3.0.0", "compile"))
				.withDependentComponent(dependencyNode("yetAnotherGroupId:yetAnotherArtifactId:jar:4.0.0", "compile"))
				.withDependentComponent(conflictingNode11)
				.withDependentComponent(conflictingNode21);
		DependencyNode dependentComponent2 = dependencyNode("someOtherGroupId:someOtherArtifactId:jar:5.0.0", "compile")
				.withDependentComponent(dependencyNode("grandChildGroupId:grandChildArtifactId:jar:2.0.0", "compile"))
				.withDependentComponent(conflictingNode12)
				.withDependentComponent(dependencyNode("anothergrandChildGroupId:anotherGrandChildArtifactId:jar:3.0.0", "compile"))
				.withDependentComponent(dependencyNode("yetAnotherGroupId:yetAnotherArtifactId:jar:4.0.0", "compile"));
		DependencyNode root = dependencyNode("rootGroupId:rootArtifactId:jar:1.0.0")
				.withDependentComponent(dependentComponent1)
				.withDependentComponent(dependentComponent2)
				.withDependentComponent(conflictingNode22)
				.withDependentComponent(conflictingNode23)
				.withDependentComponent(dependencyNode("someOtherUniqueGroupId:someOtherUniqueArtifactId:jar:4.0.0", "compile"));
		FullDependencyTree fullTree = new FullDependencyTree(root);

		Collection<ConflictingArtifact> conflictingArtifacts = new ConflictingArtifactsBuilder(fullTree, alwaysTruePredicate()).build();
		assertThat(conflictingArtifacts).hasSize(2);

		assertConflictingArtifact(conflictingArtifacts, "conflictingGroupId:conflictingArtifactId:jar", conflictingNode11,
				conflictingNode12);
		assertConflictingArtifact(conflictingArtifacts, "anotherConflictingGroupId:anotherConflictingArtifactId:jar", conflictingNode21,
				conflictingNode22, conflictingNode23);
	}

	@Test
	public void noConflictingArtifactsWhenConflictingVersionIsExcluded() {
		DependencyNode conflictingNode11 = dependencyNode("be.butskri:somethingconflicting:jar:3.0.0", "compile");
		DependencyNode conflictingNode21 = dependencyNode("be.butskri:somethingconflicting:jar:4.0.0", "compile");

		DependencyNode dependentComponent1 = dependencyNode("be.butskri:something:jar:1.0.0", "compile",
				excluding("be.butskri:somethingconflicting"))
				.withDependentComponent(conflictingNode11);
		DependencyNode dependentComponent2 = dependencyNode("be.butskri:somethingelse:jar:1.0.0", "compile")
				.withDependentComponent(conflictingNode21);
		DependencyNode root = dependencyNode("be.butskri:rootArtifactId:jar:1.0.0")
				.withDependentComponent(dependentComponent1)
				.withDependentComponent(dependentComponent2);
		FullDependencyTree fullTree = new FullDependencyTree(root);

		Collection<ConflictingArtifact> conflictingArtifacts = new ConflictingArtifactsBuilder(fullTree, alwaysTruePredicate()).build();
		assertThat(conflictingArtifacts).isEmpty();
	}

	private Predicate<DependencyNode> alwaysTruePredicate() {
		return new Predicate<DependencyNode>() {

			public boolean apply(DependencyNode input) {
				return true;
			}
		};
	}

	private void assertConflictingArtifact(Collection<ConflictingArtifact> conflictingArtifacts, String mavenArtifactId,
			DependencyNode... nodes) {
		ConflictingArtifact conflictingArtifact = findConflictingArtifactById(mavenArtifactId, conflictingArtifacts);
		assertThat(conflictingArtifact.getConflictingNodes()).containsOnly(nodes);
	}

	private ConflictingArtifact findConflictingArtifactById(String mavenArtifactId, Collection<ConflictingArtifact> conflictingArtifacts) {
		for (ConflictingArtifact conflictingArtifact : conflictingArtifacts) {
			if (conflictingArtifact.getMavenArtifactId().equals(MavenArtifactId.fromString(mavenArtifactId))) {
				return conflictingArtifact;
			}
		}
		return null;
	}

	private DependencyNode dependencyNode(String fullMavenArtifactId, String scope) {
		return new DependencyNode(FullMavenArtifactId.fromString(fullMavenArtifactId), scope,
				new HashSet<SimpleArtifactId>());
	}

	private DependencyNode dependencyNode(String fullMavenArtifactId) {
		return new DependencyNode(FullMavenArtifactId.fromString(fullMavenArtifactId),
				new HashSet<SimpleArtifactId>());
	}

	private DependencyNode dependencyNode(String fullMavenArtifactId, String scope, String... excludedArtifactIds) {
		Set<SimpleArtifactId> exclusions = excluding(excludedArtifactIds);
		return dependencyNode(fullMavenArtifactId, scope, exclusions);
	}

	private DependencyNode dependencyNode(String fullMavenArtifactId, String scope, Set<SimpleArtifactId> exclusions) {
		return new DependencyNode(FullMavenArtifactId.fromString(fullMavenArtifactId), scope, exclusions);
	}

	private Set<SimpleArtifactId> excluding(String... excludedArtifactIds) {
		Set<SimpleArtifactId> exclusions = new HashSet<SimpleArtifactId>();
		for (String excludedArtifactId : excludedArtifactIds) {
			exclusions.add(SimpleArtifactId.fromString(excludedArtifactId));
		}
		return exclusions;
	}

}
