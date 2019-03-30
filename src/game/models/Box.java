package game.models;

import java.io.Serializable;

public class Box implements Serializable {
    private int ownerId = -1;
    private BoxStatus status = BoxStatus.FREE;

    public void reset() {
        ownerId = -1;
        status = BoxStatus.FREE;
    }

    public void draw(int ownerId) {
        this.ownerId = ownerId;
        this.status = BoxStatus.TAKEN;
    }

    public void complete() {
        this.status = BoxStatus.FILLED;
    }

    public BoxStatus getStatus() {
        return status;
    }
}
