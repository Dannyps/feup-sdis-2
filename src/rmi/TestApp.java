package rmi;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import utils.ConsoleColours;
import utils.PrintMessage;

public class TestApp {

	private RMIInterface stub;

	public static void main(String[] args) {
		new TestApp(args);
	}

	private TestApp(String[] args) {
		PrintMessage.printMessages = true;
		PrintMessage.printWarnings = true;
		int nargs = args.length;
		if (nargs < 2) {
			System.err.println(ConsoleColours.YELLOW + "[ERROR] Expected at least 2 arguments, got " + nargs + ".");
			System.exit(-1);
		}

		String peer_rmi_id = args[0];
		String command = args[1];
		try {
			Registry registry = LocateRegistry.getRegistry(null);
			this.stub = (RMIInterface) registry.lookup(peer_rmi_id);

			command = command.toUpperCase();
			switch (command) {
			case "BACKUP":
				backup(args);
			case "RESTORE":
				restore(args);
				break;
			/*
			 * case "RESTORE": restore(stub, args); break; case "DELETE": delete(stub,
			 * args); break; case "RECLAIM": reclaim(stub, args); break; case "STATE":
			 * state(stub); break;
			 */
			default:
				System.err.println(ConsoleColours.YELLOW + "[ERROR] The specified sub_protocol (" + command
						+ ") could not be recognized!");
				break;
			}
		} catch (NotBoundException e) {
			System.err.println(
					ConsoleColours.RED_BOLD + "[FATAL] The specified RMI ID (" + peer_rmi_id + ") does not exist!");
			System.exit(-2);
		} catch (ConnectException e) {
			System.err.println(ConsoleColours.RED_BOLD
					+ "[FATAL] The TestApp could not connect to the rmiregistry service:\n" + e.getMessage());
			System.err.println(ConsoleColours.GREEN_BOLD_BRIGHT + "[TIP] " + ConsoleColours.YELLOW_BOLD
					+ "Perhaps the rmiregistry service is not running.");
			System.exit(-2);
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private Boolean backup(String[] args) {
		Boolean b = false;
		try {
			b = stub.backup(args[2], Integer.parseInt(args[3]));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			PrintMessage.e("Error", "backup usage: java rmi.TestApp BACKUP <filename> <replication_degree>");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (b) {
			PrintMessage.i("Success", "The backup thread has been launched successfully.");
		}
		return true;
	}

	private Object restore(String args[]) {
		Object b = false;
		try {
			b = stub.restore(args[2]);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			PrintMessage.e("Error", "backup usage: java rmi.TestApp BACKUP <filename> <replication_degree>");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (b != null) {
			PrintMessage.i("Success", "The backup thread has been launched successfully.");
		}
		return true;
	}
}