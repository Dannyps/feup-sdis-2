package utils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

//import chord.ChordController;

public class Utils {
	public static final int MAX_LENGTH_CHUNK = 64000;
	public static final short MAX_SLEEP_TIME = 400;
	public static final short BYTE_TO_KBYTE = 1000;
	public static final short RADIX = 16;

	/**
	 * Between two limits, excluding the lower one and including the upper one.
	 * 
	 * @param inf
	 * @param sup
	 * @param value
	 * @return True if value is inbetween limits.
	 */
	public static boolean isInBetween(String inf, String sup, String value) {
//		int m = ChordController.getM();
		int m = 0;
		BigInteger aux = new BigInteger((Math.pow(2, m) + "").getBytes());

		BigInteger value_tmp = new BigInteger(value, RADIX);
		BigInteger sup_tmp = new BigInteger(sup, RADIX);
		BigInteger inf_tmp = new BigInteger(inf, RADIX);

		if (value_tmp.compareTo(inf_tmp) < 0) {
			value_tmp = value_tmp.add(aux);
		}

		if (sup_tmp.compareTo(inf_tmp) <= 0) { // procura no meio do circulo todo
			sup_tmp = sup_tmp.add(aux);
		}
		return ((value_tmp.compareTo(sup_tmp) <= 0) && (inf_tmp.compareTo(value_tmp) < 0));
	}

	public static String getBiggest(String id1, String id2) {
		if (id2 == null)
			return id1;
		else if (id1 == null)
			return id2;
		else {
			if (new BigInteger(id1, RADIX).compareTo(new BigInteger(id2, RADIX)) <= 0) { // _id1 bigger than _id2
				return id2;
			} else
				return id1;
		}
	}

	public static String readFile(String path) {
		try {
			return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeToFile(Path filePath, byte[] body) throws IOException {
		if (Files.exists(filePath)) { // NOTE: O CHUNk nao Existe
			System.out.println("File already exists");
			return;
		}

		Files.createFile(filePath);
		AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.WRITE);
		CompletionHandler<Integer, ByteBuffer> writter = new CompletionHandler<Integer, ByteBuffer>() {
			@Override
			public void failed(Throwable arg0, ByteBuffer arg1) {
				System.err.println("Error: Could not write!");

			}

			@Override
			public void completed(Integer result, ByteBuffer buffer) {
				System.out.println("Finished writing!");
			}
		};
		ByteBuffer src = ByteBuffer.allocate(body.length);
		src.put(body);
		src.flip();
		asyncChannel.write(src, 0, src, writter);
	}

	public static void deleteFile(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String byteArrayToHex(byte[] a) {
		StringBuilder s = new StringBuilder();
		s.append(2 * a.length);

		for (int i = 0; i < a.length; i++)
			s.append(String.format("%02x", a[i]));

		return s.toString();
	}

	public static String getIdFromHash(byte[] hash, int length) {
		return byteArrayToHex(Arrays.copyOf(hash, length));
	}

	public static byte[] convert(List<Byte> tmp) {
		byte[] ret = new byte[tmp.size()];
		for (int i = 0; i < tmp.size(); i++) {
			ret[i] = tmp.get(i);
		}
		return ret;
	}

}