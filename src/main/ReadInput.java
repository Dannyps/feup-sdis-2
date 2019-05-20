package main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import data.BackupRequest;
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
		List<BackupRequest> backups = new LinkedList<BackupRequest>();// tem de se ir buscar os ficheiros q se fez
																		// request de backup
		if (action.equals("delete")) {
			deleteOption(backups, scanner, peer);
		} else if (action.equals("restore")) {
			restoreOption(backups, scanner, peer);
		} else {
			System.out.println("no defined action for manipulating file");
		}

	}

	private void menu() {
		StringBuilder s = new StringBuilder("Enter an option: ");
		s.append("\t0. Exit");
		s.append("\t1. Backup");
		s.append("\t2. Delete");
		s.append("\t3. Restore");

		System.out.println(s.toString());
	}

	private static String buildFileList(List<BackupRequest> requests, String action) {
		StringBuilder s = new StringBuilder();
		s.append("Select a file to ");
		s.append(action);
		s.append(":");

		int i = 0;
		for (BackupRequest backup : requests) {
			s.append(i);
			s.append(". ");
			s.append(backup.getFilename());
			s.append(" -> ");
			s.append(backup.getFileId());
			i++;
		}
		return s.toString();
	}

	private static void restoreOption(List<BackupRequest> requests, Scanner scanner, Peer peer) {
		if (!requests.isEmpty()) {
			peer.restore(requests
					.get(readListOption(scanner, buildFileList(requests, "restore"), requests.size(), "restore")));
		} else {
			System.out.println("You need to backup files before restoring");
		}
	}

	private static void deleteOption(List<BackupRequest> requests, Scanner scanner, Peer peer) {
		if (!requests.isEmpty()) {
			peer.delete(
					requests.get(readListOption(scanner, buildFileList(requests, "delete"), requests.size(), "delete"))
							.getFileId());
		} else {
			System.out.println("You need to backup files before deleting");
		}
	}

	private static void backupOption(Scanner scanner, Peer peer, short minRepDegree, short maxRepDegree) {
		System.out.println("Enter File name:");
		String filename = scanner.next();
		if (!Files.exists(Paths.get(filename))) {
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

		peer.backup(filename, degree, null);
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
