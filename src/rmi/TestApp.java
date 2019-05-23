package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

	private static RmiInterface stub;

    private TestApp() {}

	public static void main(String[] args) {

		try {
			Registry registry = LocateRegistry.createRegistry(1234);
			stub = (RmiInterface) registry.lookup("rmiTeste");
			stub.backup();



		} catch (Exception e) {
			return;
		}
	}


	// esta funcao tem de ir para o node e o node tem de extender a RmiInterface


}