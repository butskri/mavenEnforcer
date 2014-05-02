package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.List;

public interface DependencyNodeRepository {

	List<DependencyNode> findDependentComponentsFor(DependencyNode parentNode);

}
