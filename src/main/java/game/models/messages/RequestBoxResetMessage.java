package game.models.messages;

public class RequestBoxResetMessage extends Message {
    private int row;
    private int column;

    public RequestBoxResetMessage(int row, int column) {
        super(MessageType.REQUEST_BOX_RESET);

        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
