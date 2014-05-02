package be.butskri.maven.enforcer.custom.rules.domain;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.apache.maven.project.MavenProject;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class FullDependencyTreeBuilderTest {

	@Mock
	private MavenProject mavenProjectMock;
	@Mock
	private DependencyNodeRepository dependencyNodeRepositoryMock;
	@Mock
	private Predicate<MavenArtifactId> artifactsFilterMock;

	private FullDependencyTreeBuilder treeBuilder;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		treeBuilder = new FullDependencyTreeBuilder(mavenProjectMock, dependencyNodeRepositoryMock, artifactsFilterMock);

		when(artifactsFilterMock.apply(Mockito.any(MavenArtifactId.class))).thenReturn(true);

		when(mavenProjectMock.getGroupId()).thenReturn("rootGroupId");
		when(mavenProjectMock.getArtifactId()).thenReturn("rootArtifactId");
		when(mavenProjectMock.getPackaging()).thenReturn("jar");
		when(mavenProjectMock.getVersion()).thenReturn("0.0.0");
	}

	@Test
	public void childrenAreAddedToTree() {
		DependencyNode child1 = buildNode("be.butskri.child:childOne:jar:1.0.0", "compile");
		DependencyNode child11 = buildNode("be.butskri.child:childOneOne:jar:1.1.0", "compile");
		DependencyNode child12 = buildNode("be.butskri.child:childOneTwo:jar:1.2.0", "compile");
		DependencyNode child2 = buildNode("be.butskri.child:childTwo:jar:2.0.0", "compile");
		DependencyNode child21 = buildNode("be.butskri.child:childTwoOne:jar:2.1.0", "compile");
		DependencyNode child22 = buildNode("be.butskri.child:childTwoTwo:jar:2.2.0", "compile");
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(Matchers
				.argThat(hasFullMavenArtifactId("rootGroupId:rootArtifactId:jar:0.0.0")))).thenReturn(
				Lists.newArrayList(child1, child2));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child1)).thenReturn(Lists.newArrayList(child11, child12));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child2)).thenReturn(Lists.newArrayList(child21, child22));

		FullDependencyTree tree = treeBuilder.build();
		assertThat(tree.getRoot().getFullMavenArtifactId().toString()).isEqualTo("rootGroupId:rootArtifactId:jar:0.0.0");
		assertThat(tree.getRoot().getDependentComponents()).containsOnly(child1, child2);
		assertThat(child1.getDependentComponents()).containsOnly(child11, child12);
		assertThat(child2.getDependentComponents()).containsOnly(child21, child22);
	}

	@Test
	public void childrenInScopeTestAreNotAddedToTree() {
		DependencyNode child1 = buildNode("be.butskri.child:childOne:jar:1.0.0", "test");
		DependencyNode child11 = buildNode("be.butskri.child:childOneOne:jar:1.1.0", "compile");
		DependencyNode child12 = buildNode("be.butskri.child:childOneTwo:jar:1.2.0", "compile");
		DependencyNode child2 = buildNode("be.butskri.child:childTwo:jar:2.0.0", "compile");
		DependencyNode child21 = buildNode("be.butskri.child:childTwoOne:jar:2.1.0", "compile");
		DependencyNode child22 = buildNode("be.butskri.child:childTwoTwo:jar:2.2.0", "compile");
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(Matchers
				.argThat(hasFullMavenArtifactId("rootGroupId:rootArtifactId:jar:0.0.0")))).thenReturn(
				Lists.newArrayList(child1, child2));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child1)).thenReturn(Lists.newArrayList(child11, child12));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child2)).thenReturn(Lists.newArrayList(child21, child22));

		FullDependencyTree tree = treeBuilder.build();
		assertThat(tree.getRoot().getFullMavenArtifactId().toString()).isEqualTo("rootGroupId:rootArtifactId:jar:0.0.0");
		assertThat(tree.getRoot().getDependentComponents()).containsOnly(child2);
		assertThat(child1.getDependentComponents()).isEmpty();
		assertThat(child2.getDependentComponents()).containsOnly(child21, child22);
	}

	@Test
	public void filterCanExcludeChildrenFromTree() {
		DependencyNode child1 = buildNode("be.butskri.child:childOne:jar:1.0.0", "compile");
		DependencyNode child11 = buildNode("be.butskri.child:childOneOne:jar:1.1.0", "compile");
		DependencyNode child12 = buildNode("be.butskri.child:childOneTwo:jar:1.2.0", "compile");
		DependencyNode child2 = buildNode("be.butskri.child:childTwo:jar:2.0.0", "compile");
		DependencyNode child21 = buildNode("be.butskri.child:childTwoOne:jar:2.1.0", "compile");
		DependencyNode child22 = buildNode("be.butskri.child:childTwoTwo:jar:2.2.0", "compile");
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(Matchers
				.argThat(hasFullMavenArtifactId("rootGroupId:rootArtifactId:jar:0.0.0")))).thenReturn(
				Lists.newArrayList(child1, child2));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child1)).thenReturn(Lists.newArrayList(child11, child12));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child2)).thenReturn(Lists.newArrayList(child21, child22));
		when(artifactsFilterMock.apply(MavenArtifactId.fromString("be.butskri.child:childOne:jar"))).thenReturn(false);

		FullDependencyTree tree = treeBuilder.build();
		assertThat(tree.getRoot().getFullMavenArtifactId().toString()).isEqualTo("rootGroupId:rootArtifactId:jar:0.0.0");
		assertThat(tree.getRoot().getDependentComponents()).containsOnly(child2);
		assertThat(child1.getDependentComponents()).isEmpty();
		assertThat(child2.getDependentComponents()).containsOnly(child21, child22);
	}

	@Test
	public void childrenOfExcludedNodesAreNotAdded() {
		DependencyNode child1 = buildNode("be.butskri.child:childOne:jar:1.0.0", "compile");
		DependencyNode child11 = buildNode("be.butskri.child:childOneOne:jar:1.1.0", "compile");
		DependencyNode child12 = buildNode("be.butskri.child:childOneTwo:jar:1.2.0", "compile");
		DependencyNode child2 = buildNode("be.butskri.child:childTwo:jar:2.0.0", "compile",
				SimpleArtifactId.fromString("be.butskri.child:childTwoOne"));
		DependencyNode child21 = buildNode("be.butskri.child:childTwoOne:jar:2.1.0", "compile");
		DependencyNode child211 = buildNode("be.butskri.child:childTwoOneOne:jar:2.1.1", "compile");
		DependencyNode child22 = buildNode("be.butskri.child:childTwoTwo:jar:2.2.0", "compile");
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(Matchers
				.argThat(hasFullMavenArtifactId("rootGroupId:rootArtifactId:jar:0.0.0")))).thenReturn(
				Lists.newArrayList(child1, child2));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child1)).thenReturn(Lists.newArrayList(child11, child12));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child2)).thenReturn(Lists.newArrayList(child21, child22));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child21)).thenReturn(Lists.newArrayList(child211));

		FullDependencyTree tree = treeBuilder.build();
		assertThat(tree.getRoot().getFullMavenArtifactId().toString()).isEqualTo("rootGroupId:rootArtifactId:jar:0.0.0");
		assertThat(tree.getRoot().getDependentComponents()).containsOnly(child1, child2);
		assertThat(child1.getDependentComponents()).containsOnly(child11, child12);
		assertThat(child2.getDependentComponents()).containsOnly(child21, child22);
		assertThat(child21.getDependentComponents()).isEmpty();
	}

	@Test
	public void childrenOfNodeWithVersionRangeAreNotAdded() {
		DependencyNode child1 = buildNode("be.butskri.child:childOne:jar:1.0.0", "compile");
		DependencyNode child11 = buildNode("be.butskri.child:childOneOne:jar:1.1.0", "compile");
		DependencyNode child12 = buildNode("be.butskri.child:childOneTwo:jar:1.2.0", "compile");
		DependencyNode child2 = buildNode("be.butskri.child:recursiveNode:jar:someVersion", "compile");
		DependencyNode child21 = buildNode("be.butskri.child:childTwoOne:jar:2.1.0", "compile");
		DependencyNode child211 = buildNode("be.butskri.child:recursiveNode:jar:someVersion", "compile");
		DependencyNode child2111 = buildNode("be.butskri.child:childTwoOneOneOne:jar:2.1.1.1", "compile");
		DependencyNode child22 = buildNode("be.butskri.child:childTwoTwo:jar:2.2.0", "compile");
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(Matchers
				.argThat(hasFullMavenArtifactId("rootGroupId:rootArtifactId:jar:0.0.0")))).thenReturn(
				Lists.newArrayList(child1, child2));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child1)).thenReturn(Lists.newArrayList(child11, child12));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child2)).thenReturn(Lists.newArrayList(child21, child22));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child21)).thenReturn(Lists.newArrayList(child211));
		when(dependencyNodeRepositoryMock.findDependentComponentsFor(child211)).thenReturn(Lists.newArrayList(child2111));

		FullDependencyTree tree = treeBuilder.build();
		assertThat(tree.getRoot().getFullMavenArtifactId().toString()).isEqualTo("rootGroupId:rootArtifactId:jar:0.0.0");
		assertThat(tree.getRoot().getDependentComponents()).containsOnly(child1, child2);
		assertThat(child1.getDependentComponents()).containsOnly(child11, child12);
		assertThat(child2.getDependentComponents()).containsOnly(child21, child22);
		assertThat(child21.getDependentComponents()).containsOnly(child211);
		assertThat(child211.getDependentComponents()).isEmpty();
	}

	private Matcher<DependencyNode> hasFullMavenArtifactId(final String fullMavenArtifactId) {
		return new BaseMatcher<DependencyNode>() {

			public boolean matches(Object item) {
				DependencyNode node = (DependencyNode) item;
				return node.getFullMavenArtifactId().equals(FullMavenArtifactId.fromString(fullMavenArtifactId));
			}

			public void describeTo(Description description) {
				description.appendText("hasFullMavenArtifactId(" + fullMavenArtifactId + ")");
			}
		};
	}

	private DependencyNode buildNode(String fullMavenArtifactId, String scope, SimpleArtifactId... exclusions) {
		return new DependencyNode(FullMavenArtifactId.fromString(fullMavenArtifactId), scope, Sets.newHashSet(exclusions));
	}
}
