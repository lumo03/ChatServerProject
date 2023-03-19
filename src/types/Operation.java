package types;

public enum Operation {
	TEXT, CMD_RENAME, CMD_QUIT, CMD_JOIN, CMD_ADD_TO_ORDER, CMD_REQUEST_ORDER, START_ORDER;
	
	@Override
	public String toString() {
		switch (this) {
			case TEXT:
				return "Text";
			case CMD_RENAME:
				return "Rename";
			case CMD_QUIT:
				return "Quit";
			case CMD_JOIN:
				return "Join";
			case CMD_ADD_TO_ORDER:
				return "Order";
			case CMD_REQUEST_ORDER:
				return "Request Order";
			case START_ORDER:
				return "Start Order";
			default:
				throw new IllegalArgumentException();
		}
	}
}
