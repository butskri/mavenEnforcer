package be.butskri.maven.enforcer.custom.rules.aether;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.repository.RemoteRepository;

public class RemoteRepositoryFactory {

	public static List<RemoteRepository> getRepositories() {
		return new ArrayList<RemoteRepository>(Arrays.asList(newCentralRepository()));
	}

	private static RemoteRepository newCentralRepository() {
		return new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/").build();
	}
}