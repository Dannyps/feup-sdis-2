package main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import controller.Controller;
import peer.Peer;

public class ReadInput {

	private Peer peer;
	private Scanner scanner;

	public ReadInput(Peer peer) {
		this.peer = peer;
		this.scanner = new Scanner(System.in);
	}

	public void run() {
		boolean run;
		do {
			run = readOption();
		} while (run);
		scanner.close();
	}

	private boolean readOption() {
		Integer op = null;
		boolean ok = true;
		try {
			menu();
			op = scanner.nextInt();
		} catch (InputMismatchException e) {
			System.out.println("Invalid Input");
			scanner.nextLine();
			ok = false;
		} catch (Exception e) {
			e.printStackTrace();
			scanner.nextLine();
			ok = false;
		}

		if (!ok)
			return false;

		return processOption(op);
	}

	private boolean processOption(Integer option) {
		boolean ok = true;
		if (option == 1) {
			ReadInput.backupOption(scanner, peer, (short) 1, (short) 9);
		} else if (option == 2) {
			ReadInput.manipulateFile("delete", scanner, peer);
		} else if (option == 3) {
			ReadInput.manipulateFile("restore", scanner, peer);
		} else if (option == 0) {
			Thread.currentThread().interrupt();
		} else {
			System.out.println("Error: not a valid input!");
			ok = false;
		}
		return ok;
	}

	private static void manipulateFile(String action, Scanner scanner, Peer peer) {
		List<String> backups = Collections.list(Controller.getInstance().getBackupController().getFileNames());
		if (action.equals("delete")) {
			deleteOption(backups, scanner, peer);
		} else if (action.equals("restore")) {
			restoreOption(backups, scanner, peer);
		} else {
			System.out.println("no defined action for manipulating file");
		}

	}

	private void menu() {
		StringBuilder s = new StringBuilder("Enter an option: \n");
		s.append("\t0. Exit\n");
		s.append("\t1. Backup\n");
		s.append("\t2. Delete\n");
		s.append("\t3. Restore\n");

		System.out.println(s.toString());
	}

	private static String buildFileList(List<String> requests, String action) {
		StringBuilder s = new StringBuilder();
		s.append("Select a file to ");
		s.append(action);
		s.append(":\n");

		int i = 0;
		for (String backup : requests) {
			s.append(i);
			s.append(". ");
			s.append(backup);
//			s.append(backup.getFilename());
//			s.append(" -> ");
//			s.append(backup.getFileId());
			i++;
		}
		return s.toString();
	}

	private static void restoreOption(List<String> requests, Scanner scanner, Peer peer) {
		if (!requests.isEmpty()) {
			peer.restore(requests
					.get(readListOption(scanner, buildFileList(requests, "restore"), requests.size(), "restore")));
		} else {
			System.out.println("You need to backup files before restoring");
		}
	}

	private static void deleteOption(List<String> requests, Scanner scanner, Peer peer) {
		if (!requests.isEmpty()) {
			peer.delete(requests
					.get(readListOption(scanner, buildFileList(requests, "delete"), requests.size(), "delete")));
		} else {
			System.out.println("You need to backup files before deleting");
		}
	}

	private static void backupOption(Scanner scanner, Peer peer, short minRepDegree, short maxRepDegree) {
		System.out.println("Enter File name:");
		String fileName = scanner.next();
		if (!Files.exists(Paths.get(fileName))) {
			System.out.println("Error: file does not exist!");
			return;
		}

		Short degree = 0;
		do {
			StringBuilder s = new StringBuilder("Replication Degree (");
			s.append(minRepDegree);
			s.append("-");
			s.append(maxRepDegree);
			s.append("):");
			System.out.println(s.toString());

			degree = readOption(scanner);
		} while ((degree < minRepDegree) || (degree > maxRepDegree));

		peer.backup(fileName, degree, null, fileName);
		System.out.println("Called Backup!");
	}

	private static int readListOption(Scanner scanner, String fileList, int filesSize, String action) {
		short option = -1;
		do {
			System.out.println(fileList);
			option = readOption(scanner);
		} while (option < 0 || option >= filesSize);
		return option;
	}

	private static short readOption(Scanner scanner) {
		try {
			return scanner.nextShort();
		} catch (InputMismatchException e) {
			System.out.println("Invalid Input");
			scanner.nextLine();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

}
