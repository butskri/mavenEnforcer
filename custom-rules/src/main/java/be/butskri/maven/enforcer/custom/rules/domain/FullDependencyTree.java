package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.Collection;

import org.apache.maven.plugin.logging.Log;

import be.butskri.maven.enforcer.custom.rules.appender.LogLevel;
import be.butskri.maven.enforcer.custom.rules.appender.LoggingStringAppender;

public class FullDependencyTree {

	private DependencyNode root;

	public FullDependencyTree(DependencyNode root) {
		this.root = root;
	}

	public DependencyNode getRoot() {
		return root;
	}

	public void print(Log log, LogLevel logLevel) {
		root.print(new LoggingStringAppender(log, logLevel));
	}

	public Collection<DependencyNode> getAllDependentNodes() {
		return root.getAllDependentNodes();
	}
}
