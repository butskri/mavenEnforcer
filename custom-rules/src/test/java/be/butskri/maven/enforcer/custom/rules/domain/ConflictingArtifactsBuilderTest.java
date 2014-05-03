package be.butskri.maven.enforcer.custom.rules.domain;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class ConflictingArtifactsBuilderTest {

	private static final String ROOT_FULL_MAVEN_ARTIFACT_ID = "be.butskri.example:rootArtifactId:jar:1.0.0";

	@Mock
	private MavenProject mavenProjectMock;
	@Mock
	private DependencyManagement dependencyManagementMock;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mavenProjectMock.getDependencyManagement()).thenReturn(dependencyManagementMock);
		when(mavenProjectMock.getGroupId()).thenReturn("be.butskri.example");
		when(mavenProjectMock.getArtifactId()).thenReturn("rootArtifactId");
		when(mavenProjectMock.getPackaging()).thenReturn("jar");
		when(mavenProjectMock.getVersion()).thenReturn("1.0.0");
	}

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

		Collection<ConflictingArtifact> conflictingArtifacts = new ConflictingArtifactsBuilder(mavenProjectMock, fullTree,
				alwaysTruePredicate()).build();
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

		Collection<ConflictingArtifact> conflictingArtifacts = new ConflictingArtifactsBuilder(mavenProjectMock, fullTree,
				alwaysTruePredicate()).build();
		assertThat(conflictingArtifacts).isEmpty();
	}

	@Test
	public void dependencyManagementIsAlsoTakenIntoAccountWhenFindingConflictingVersions() {
		Dependency dependency = dependency("be.butskri.example", "c", "jar", "2.0.0");
		when(dependencyManagementMock.getDependencies()).thenReturn(Lists.newArrayList(dependency));
		DependencyNode componentC = dependencyNode("be.butskri.example:c:jar:1.0.0", "compile");
		DependencyNode componentA = dependencyNode("be.butskri.example:a:jar:1.0.0", "compile")
				.withDependentComponent(dependencyNode("be.butskri.example:b:jar:1.0.0", "compile")
						.withDependentComponent(componentC));
		DependencyNode root = dependencyNode(ROOT_FULL_MAVEN_ARTIFACT_ID)
				.withDependentComponent(componentA);
		FullDependencyTree fullTree = new FullDependencyTree(root);

		Collection<ConflictingArtifact> conflictingArtifacts = new ConflictingArtifactsBuilder(mavenProjectMock, fullTree,
				alwaysTruePredicate()).build();
		assertThat(conflictingArtifacts).hasSize(1);

		ConflictingArtifact conflictingArtifact = conflictingArtifacts.iterator().next();
		Collection<DependencyNode> conflictingNodes = conflictingArtifact.getConflictingNodes();
		assertThat(conflictingNodes).hasSize(2);
		assertThat(conflictingNodes).contains(componentC);
		DependencyNode depMgtComponentC = findNodeById(conflictingNodes, "be.butskri.example:c:jar:2.0.0");
		assertThat(depMgtComponentC.getScope()).isEqualTo("depMgt");
		assertThat(depMgtComponentC.getParentNode().getFullMavenArtifactId().toString()).isEqualTo(ROOT_FULL_MAVEN_ARTIFACT_ID);
		assertThat(depMgtComponentC.getParentNode().getParentNode()).isNull();
	}

	private DependencyNode findNodeById(Collection<DependencyNode> nodes, String fullMavenArtifactId) {
		for (DependencyNode dependencyNode : nodes) {
			if (dependencyNode.getFullMavenArtifactId().toString().equals(fullMavenArtifactId)) {
				return dependencyNode;
			}
		}
		return null;
	}

	private Dependency dependency(String groupId, String artifactId, String type, String version) {
		Dependency result = new Dependency();
		result.setGroupId(groupId);
		result.setArtifactId(artifactId);
		result.setType(type);
		result.setVersion(version);
		return result;
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
