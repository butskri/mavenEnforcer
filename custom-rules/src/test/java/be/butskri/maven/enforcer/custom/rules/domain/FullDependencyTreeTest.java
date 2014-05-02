package be.butskri.maven.enforcer.custom.rules.domain;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mockito.Mockito;

import be.butskri.maven.enforcer.custom.rules.appender.LogLevel;

public class FullDependencyTreeTest {

	@Test
	public void print() {
		Log logMock = Mockito.mock(Log.class);
		DependencyNode root = dependencyNode("be.butskri.maven.enforcer:project-x:jar:0.0.1-SNAPSHOT", "unused")
				.withDependentComponent(dependencyNode("be.butskri.maven.enforcer:project-y:jar:0.0.1-SNAPSHOT", "compile")
						.withDependentComponent(dependencyNode("com.google.guava:guava:jar:12.0", "runtime")
								.withDependentComponent(dependencyNode("com.google.code.findbugs:jsr305:jar:1.3.9", "runtime"))))
				.withDependentComponent(dependencyNode("be.butskri.maven.enforcer:project-z:jar:0.0.1-SNAPSHOT", "compile"))
				.withDependentComponent(
						dependencyNode("org.apache.maven:maven-aether-provider:jar:3.2.1", "compile")
								.withDependentComponent(dependencyNode("org.apache.maven:maven-model:jar:3.2.1", "compile"))
								.withDependentComponent(dependencyNode("org.apache.maven:maven-model-builder:jar:3.2.1", "compile")
										.withDependentComponent(
												dependencyNode("org.codehaus.plexus:plexus-interpolation:jar:1.19", "compile")))
								.withDependentComponent(dependencyNode("org.apache.maven:maven-repository-metadata:jar:3.2.1", "compile"))
								.withDependentComponent(dependencyNode("org.codehaus.plexus:plexus-utils:jar:3.0.17", "compile")))
				.withDependentComponent(dependencyNode("junit:junit:jar:3.8.1", "test"));

		FullDependencyTree fullDependencyTree = new FullDependencyTree(root);
		fullDependencyTree.print(logMock, LogLevel.DEBUG);

		verify(logMock).debug("be.butskri.maven.enforcer:project-x:jar:0.0.1-SNAPSHOT");
		verify(logMock).debug("+- be.butskri.maven.enforcer:project-y:jar:0.0.1-SNAPSHOT:compile");
		verify(logMock).debug("|  \\- com.google.guava:guava:jar:12.0:runtime");
		verify(logMock).debug("|     \\- com.google.code.findbugs:jsr305:jar:1.3.9:runtime");
		verify(logMock).debug("+- be.butskri.maven.enforcer:project-z:jar:0.0.1-SNAPSHOT:compile");
		verify(logMock).debug("+- org.apache.maven:maven-aether-provider:jar:3.2.1:compile");
		verify(logMock).debug("|  +- org.apache.maven:maven-model:jar:3.2.1:compile");
		verify(logMock).debug("|  +- org.apache.maven:maven-model-builder:jar:3.2.1:compile");
		verify(logMock).debug("|  |  \\- org.codehaus.plexus:plexus-interpolation:jar:1.19:compile");
		verify(logMock).debug("|  +- org.apache.maven:maven-repository-metadata:jar:3.2.1:compile");
		verify(logMock).debug("|  \\- org.codehaus.plexus:plexus-utils:jar:3.0.17:compile");
		verify(logMock).debug("\\- junit:junit:jar:3.8.1:test");
		verifyNoMoreInteractions(logMock);
	}

	private DependencyNode dependencyNode(String fullMavenArtifactId, String scope) {
		return new DependencyNode(FullMavenArtifactId.fromString(fullMavenArtifactId), scope, new HashSet<SimpleArtifactId>());
	}

}
