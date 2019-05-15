package chord.peer_info;

import java.net.InetAddress;

public class NullPeerInfo extends AbstractPeerInfo {

	public NullPeerInfo() {
		super(null, null, null);
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public InetAddress getAddr() {
		return null;
	}

	@Override
	public Integer getPort() {
		return null;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String[] asArray() {
		return new String[] { "null" };
	}

}
