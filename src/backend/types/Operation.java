package backend.types;

public enum Operation {
    TEXT, CMD_RENAME, CMD_QUIT, CMD_JOIN, CMD_ADD_TO_ORDER, CMD_REQUEST_ORDER, START_ORDER, ANNOUNCE_DELIVERY_STARTED, ANNOUNCE_DELIVERY_FINISHED;

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
            case ANNOUNCE_DELIVERY_STARTED:
                return "Announce Delivery Started";
            case ANNOUNCE_DELIVERY_FINISHED:
                return "Announce Delivery Finished";
            default:
                throw new IllegalArgumentException();
        }
    }
}
