package game.models;

public class UpdateStateMessage extends Message {
    private GameState state;

    public UpdateStateMessage(GameState state) {
        super(MessageType.UPDATE_STATE);

        this.state = state;
    }

    public GameState getState() {
        return state;
    }
}
