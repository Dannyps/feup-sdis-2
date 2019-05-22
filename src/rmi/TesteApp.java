package rmi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import message.Message;
import message.MessageType;
import utils.AddrPort;

public class TesteApp {

	private static RmiInterface stub;

    private TesteApp() {}

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