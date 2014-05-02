package be.butskri.maven.enforcer.custom.rules.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class CompositeDependencyNodeRepository implements DependencyNodeRepository {

	private List<DependencyNodeRepository> repositories = new ArrayList<DependencyNodeRepository>();

	public CompositeDependencyNodeRepository(DependencyNodeRepository... dependencyNodeRepositories) {
		this.repositories = Lists.newArrayList(dependencyNodeRepositories);
	}

	public List<DependencyNode> findDependentComponentsFor(DependencyNode parentNode) {
		for (DependencyNodeRepository repository : repositories) {
			List<DependencyNode> result = repository.findDependentComponentsFor(parentNode);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}
