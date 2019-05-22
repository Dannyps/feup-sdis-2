package communication;

public enum MessageType {

	PUTCHUNK("PUTCHUNK"),
	KEEPCHUNK("KEEPCHUNK"),
	STORED("STORED"),
	DELETE("DELETE"), 
	INITDELETE("INITDELETE"),
	GETCHUNK("GETCHUNK"), 
	CHUNK("CHUNK");

	private String type;

	MessageType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
