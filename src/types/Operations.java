package types;

public enum Operations {
	TEXT, CMD_RENAME, CMD_QUIT, CMD_JOIN, CMD_ORDER;
	
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
			case CMD_ORDER:
				return "Order";
			default:
				throw new IllegalArgumentException();
		}
	}
}
