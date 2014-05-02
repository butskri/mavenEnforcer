package be.butskri.maven.enforcer.custom.rules.domain;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

public class DependencyNodeTest {

	@Test
	public void rootNodeIsNeverExcluded() {
		Set<SimpleArtifactId> exclusions = Sets.newHashSet(SimpleArtifactId.fromString("be.butskri.myGroupId:someArtifactId"),
				SimpleArtifactId.fromString("be.butskri.myGroupId:someOtherArtifactId"));
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", exclusions);

		assertThat(parentNode.isExcluded()).isFalse();
	}

	@Test
	public void childNodeIsNotExcludedWhenSimpleNameDoesntMatchAnyExcludedSimpleArtifactId() {
		Set<SimpleArtifactId> exclusions = Sets.newHashSet(SimpleArtifactId.fromString("be.butskri.myGroupId:someArtifactId"),
				SimpleArtifactId.fromString("be.butskri.myGroupId:someOtherArtifactId"));
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", exclusions);
		DependencyNode childNode = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someChildArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someGrandChildArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		parentNode.withDependentComponent(childNode);
		childNode.withDependentComponent(grandChildNode);

		assertThat(childNode.isExcluded()).isFalse();
		assertThat(grandChildNode.isExcluded()).isFalse();
	}

	@Test
	public void childNodeIsExcludedWhenSimpleNameMatchesAnExcludedSimpleArtifactId() {
		Set<SimpleArtifactId> exclusions = Sets.newHashSet(SimpleArtifactId.fromString("be.butskri.myGroupId:someArtifactId"),
				SimpleArtifactId.fromString("be.butskri.myGroupId:someExcludedArtifactId"));
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", exclusions);
		DependencyNode childNode = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someExcludedArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		parentNode.withDependentComponent(childNode);

		assertThat(childNode.isExcluded()).isTrue();
	}

	@Test
	public void grandChildNodeIsExcludedWhenParentIsExcluded() {
		Set<SimpleArtifactId> exclusions = Sets.newHashSet(SimpleArtifactId.fromString("be.butskri.myGroupId:someArtifactId"),
				SimpleArtifactId.fromString("be.butskri.myGroupId:someExcludedArtifactId"));
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", exclusions);
		DependencyNode childNode = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someExcludedArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someGrandChildArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		parentNode.withDependentComponent(childNode);
		childNode.withDependentComponent(grandChildNode);

		assertThat(grandChildNode.isExcluded()).isTrue();
	}

	@Test
	public void nodeIsNotExcludedWhenExcludingOwnSimpleArtifactId() {
		Set<SimpleArtifactId> exclusions = Sets.newHashSet(SimpleArtifactId.fromString("be.butskri.myGroupId:someArtifactId"),
				SimpleArtifactId.fromString("be.butskri.myGroupId:someExcludedArtifactId"));
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode childNode = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someExcludedArtifactId:jar:1.0.0"),
				"compile", exclusions);
		parentNode.withDependentComponent(childNode);

		assertThat(childNode.isExcluded()).isFalse();
	}

	@Test
	public void hasInPathOnlyReturnsTrueWhenGivenIdInPath() {
		FullMavenArtifactId fullChild2Id = FullMavenArtifactId.fromString("be.butskri.myGroupId:someOtherChildArtifactId:jar:2.0.0");
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode childNode1 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someChildArtifactId:jar:2.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode1 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someGrandChildArtifactId:jar:3.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode childNode2 = new DependencyNode(
				fullChild2Id,
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode2 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someOtherGrandChildArtifactId:jar:3.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		parentNode.withDependentComponent(childNode1);
		parentNode.withDependentComponent(childNode2);
		childNode1.withDependentComponent(grandChildNode1);
		childNode2.withDependentComponent(grandChildNode2);

		assertThat(parentNode.hasInPath(fullChild2Id)).isFalse();
		assertThat(childNode1.hasInPath(fullChild2Id)).isFalse();
		assertThat(grandChildNode1.hasInPath(fullChild2Id)).isFalse();
		assertThat(childNode2.hasInPath(fullChild2Id)).isTrue();
		assertThat(grandChildNode2.hasInPath(fullChild2Id)).isTrue();
	}

	@Test
	public void getAllDependentNodesReturnsAllDependentNodes() {
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode childNode1 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someChildArtifactId:jar:2.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode1 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someGrandChildArtifactId:jar:3.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode childNode2 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someOtherChildArtifactId:jar:2.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode2 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someOtherGrandChildArtifactId:jar:3.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		parentNode.withDependentComponent(childNode1);
		parentNode.withDependentComponent(childNode2);
		childNode1.withDependentComponent(grandChildNode1);
		childNode2.withDependentComponent(grandChildNode2);

		Collection<DependencyNode> allDependentNodes = parentNode.getAllDependentNodes();
		assertThat(allDependentNodes).containsOnly(childNode1, grandChildNode1, childNode2, grandChildNode2);
	}

	@Test
	public void getAllDependentNodesExcludesExcludedNodes() {
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet(SimpleArtifactId.fromString("be.butskri.myGroupId:someGrandChildArtifactId"),
						SimpleArtifactId.fromString("be.butskri.myGroupId:someOtherChildArtifactId")));
		DependencyNode childNode1 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someChildArtifactId:jar:2.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode1 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someGrandChildArtifactId:jar:3.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode childNode2 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someOtherChildArtifactId:jar:2.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode2 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someOtherGrandChildArtifactId:jar:3.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		parentNode.withDependentComponent(childNode1);
		parentNode.withDependentComponent(childNode2);
		childNode1.withDependentComponent(grandChildNode1);
		childNode2.withDependentComponent(grandChildNode2);

		Collection<DependencyNode> allDependentNodes = parentNode.getAllDependentNodes();
		assertThat(allDependentNodes).containsOnly(childNode1);
	}

	@Test
	public void testGetPath() {
		DependencyNode parentNode = new DependencyNode(FullMavenArtifactId.fromString("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode childNode1 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someChildArtifactId:jar:2.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode1 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someGrandChildArtifactId:jar:3.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode childNode2 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someOtherChildArtifactId:jar:2.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		DependencyNode grandChildNode2 = new DependencyNode(
				FullMavenArtifactId.fromString("be.butskri.myGroupId:someOtherGrandChildArtifactId:jar:3.0.0"),
				"compile", Sets.<SimpleArtifactId> newHashSet());
		parentNode.withDependentComponent(childNode1);
		parentNode.withDependentComponent(childNode2);
		childNode1.withDependentComponent(grandChildNode1);
		childNode2.withDependentComponent(grandChildNode2);

		String path = grandChildNode1.getPath();
		assertThat(path).isEqualTo("be.butskri.myGroupId:myParentArtifactId:jar:1.0.0\n" +
				"\\- be.butskri.myGroupId:someChildArtifactId:jar:2.0.0:compile\n" +
				"   \\- be.butskri.myGroupId:someGrandChildArtifactId:jar:3.0.0:compile\n");
	}

}
