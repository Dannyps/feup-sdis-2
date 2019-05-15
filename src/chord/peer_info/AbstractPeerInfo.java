package chord.peer_info;

import java.net.InetAddress;

public abstract class AbstractPeerInfo {

	protected String id;
	protected InetAddress addr;
	protected Integer port;

	public AbstractPeerInfo(String id, InetAddress addr, Integer port) {
		this.id = id;
		this.addr = addr;
		this.port = port;
	}

	public abstract boolean isNull();

	public abstract InetAddress getAddr();

	public abstract Integer getPort();

	public abstract String getId();

	public abstract String[] asArray();
}
