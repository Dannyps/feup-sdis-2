package communication;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageFactory {

	private static String END_HEADER = "\r\n\r\n";
	private static String NEW_LINE = "\r\n";
	private static String SPACE_SEPARATOR = " ";
	private static String VERSION = "1.0";
	private static String VERSION_SECOND = "2.0";

	private static short IDX_ID = 0;
	private static short IDX_ADDRESS = 1;
	private static short IDX_PORT = 2;
	private static short STEP = 3;

	public static String getFirstLine(MessageType messageType, String version, String id) {
		StringBuilder s = new StringBuilder(messageType.getType());
		s.append(SPACE_SEPARATOR);
		s.append(version);
		s.append(SPACE_SEPARATOR);
		s.append(id);
		s.append(SPACE_SEPARATOR);
		s.append(NEW_LINE);

		return s.toString();
	}

	public static String getHeader(MessageType messageType, String version, String senderId) {
		StringBuilder s = new StringBuilder(getFirstLine(messageType, version, senderId));
		s.append(NEW_LINE);
		return s.toString();
	}

	public static String appendLine(String message, Object args[]) {
		StringBuilder s = new StringBuilder(message);
		for (Object arg : args) {
			s.append(arg.toString());
			s.append(SPACE_SEPARATOR);
		}
		s.append(END_HEADER);
		return s.toString();
	}

	public static String appendBody(String message, byte[] body) throws UnsupportedEncodingException {
		String bodyStr = new String(body, StandardCharsets.ISO_8859_1);
		message += bodyStr;
		return message;
	}

	public static String getPutChunk(String id, InetAddress addr, int port, String fileID, int chunkNo,
			int replicationDeg, byte[] body, String encryptKey, String fileName) {
		String msg = appendLine(getFirstLine(MessageType.PUTCHUNK, VERSION, id), new Object[] { id,
				addr.getHostAddress(), port, fileID, chunkNo, replicationDeg, encryptKey, fileName });
		try {
			return appendBody(msg, body);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getGetChunk(String senderId, InetAddress addr, int port, String fileID, int chunkNo) {
		return appendLine(getFirstLine(MessageType.GETCHUNK, VERSION, senderId),
				new Object[] { addr.getHostAddress(), port, fileID, chunkNo });
	}

	public static String getDelete(String senderId, String fileId, int repDegree) {
		return appendLine(getFirstLine(MessageType.DELETE, VERSION, senderId), new Object[] { fileId, repDegree });
	}

	public static String getStored(String senderId, String fileID, int chunkNo, int replicationDeg) {
		return appendLine(getFirstLine(MessageType.STORED, VERSION, senderId),
				new Object[] { fileID, chunkNo, replicationDeg });
	}

	public static String getInitDelete(String senderId, String fileId) {
		return appendLine(getFirstLine(MessageType.INITDELETE, VERSION, senderId), new Object[] { fileId });
	}

	public static String getChunk(String senderId, String fileID, int chunkNo, byte[] body, String encryptionKey,
			String fileName) {
		String msg = appendLine(getFirstLine(MessageType.CHUNK, VERSION, senderId),
				new Object[] { fileID, chunkNo, encryptionKey, fileName });
		try {
			return appendBody(msg, body);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getKeepChunk(String senderId, InetAddress addr, int port, String fileID, int chunkNo,
			int replicationDeg, byte[] body, String encryption_key, String fileName) {
		String msg = appendLine(getFirstLine(MessageType.KEEPCHUNK, VERSION, senderId), new Object[] { senderId,
				addr.getHostAddress(), port, fileID, chunkNo, replicationDeg, encryption_key, fileName });
		try {
			return appendBody(msg, body);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
