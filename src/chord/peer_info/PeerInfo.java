/**
 * 
 */
package chord.peer_info;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PeerInfo extends AbstractPeerInfo {
	private static final String DELIMITER = "\r\n";

	public PeerInfo(String id, InetAddress addr, Integer port) {
		super(id, addr, port);
	}

	public PeerInfo(String str) {
		super(null, null, null);
		String[] attr = str.trim().split(DELIMITER);

		if (!setAttributes(attr[1].split(" ")))
			return;
	}

	private boolean setAttributes(String[] attr) {
		id = attr[0];

		if (!parseAdress(attr[1]))
			return false;

		this.port = Integer.valueOf(attr[2]);

		return true;
	}

	private boolean parseAdress(String elem) {
		boolean flag = true;
		try {
			this.addr = InetAddress.getByName(elem);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			flag = false;
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	public InetAddress getAddr() {
		return addr;
	}

	@Override
	public String[] asArray() {
		return new String[] { id, addr.getHostAddress(), port.toString() };
	}

	/**
	 * @param addr the addr to set
	 */
	public void setAddr(InetAddress addr) {
		this.addr = addr;
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof PeerInfo))
			return false;
		PeerInfo otherPeer = (PeerInfo) other;
		if (otherPeer.getId().equals(this.id)) {
			return true;
		}
		return false;
	}

}
