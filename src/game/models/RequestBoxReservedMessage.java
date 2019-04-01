package game.models;

public class RequestBoxReservedMessage extends Message {
    private int row;
    private int column;
    private int ownerId;
    private String boxColorString;

    public RequestBoxReservedMessage(int row, int column, int ownerId, String boxColorString) {
        super(MessageType.REQUEST_BOX_RESERVED);

        this.row = row;
        this.column = column;
        this.ownerId = ownerId;
        this.boxColorString = boxColorString;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getBoxColorString() {
        return boxColorString;
    }
}
