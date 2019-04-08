package game.views;

import game.Main;
import game.models.Box;
import game.models.BoxStatus;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class BoxView extends StackPane {

    private final double boxWidth = 50;
    private final double boxHeight = 50;
    private final Color whiteColor = Color.web("#ffffff");

    private int row;
    private int column;
    private double minPercentage;
    private double lineWidth;
    private Color currentPlayerColor;
    private int currentPlayerId;
    private Canvas canvas;
    private GraphicsContext gc;

    private boolean drawing = false;
    private double previousX = -1;
    private double previousY = -1;


    public BoxView(int row, int column) {
        super();

        this.row = row;
        this.column = column;
        this.minPercentage = Main.getState().getMinPercentage();
        this.lineWidth = Main.getState().getLineWidth();
        this.currentPlayerColor = Main.getCurrentPlayer().getColor();
        this.currentPlayerId = Main.getCurrentPlayer().getId();

        canvas = new Canvas(boxWidth, boxHeight);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);

        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(lineWidth);

        fill(whiteColor);

        this.getChildren().add(canvas);
        this.setStyle("-fx-border-color: black; -fx-border-width: 1;");
    }

    private synchronized void onMousePressed(MouseEvent e) {
        BoxStatus boxStatus = Main.getState().getBox(row, column).getStatus();

        // If the box is free
        if (boxStatus.equals(BoxStatus.FREE)) {
            drawing = true;
            double x = e.getX();
            double y = e.getY();

            draw(x, y, x, y, currentPlayerColor);
            Main.onBoxReserved(row, column);
        }
    }

    private synchronized void onMouseDragged(MouseEvent e) {
        Box box = Main.getState().getBox(row, column);
        int ownerId = box.getOwnerId();
        BoxStatus boxStatus = box.getStatus();

        // If the box is free or reserved by current player
        if (boxStatus.equals(BoxStatus.FREE) || (boxStatus.equals(BoxStatus.RESERVED) && ownerId == currentPlayerId)) {
            double x = e.getX();
            double y = e.getY();

            // If previous and current cursor positions are within the box
            if (drawing && previousX > -1 && previousY > -1 && x > 0 && x < boxWidth && y > 0 && y < boxHeight) {
                draw(previousX, previousY, x, y, currentPlayerColor);
            }
        }
    }

    private synchronized void onMouseReleased(MouseEvent e) {
        gc.setStroke(currentPlayerColor);

        Box box = Main.getState().getBox(row, column);
        int ownerId = box.getOwnerId();
        BoxStatus boxStatus = box.getStatus();

        // If the box is free or reserved by current player
        if (boxStatus.equals(BoxStatus.FREE) || (boxStatus.equals(BoxStatus.RESERVED) && ownerId == currentPlayerId)) {
            if (drawing) {
                drawing = false;
                previousX = -1;
                previousY = -1;

                double currentPercentage = getCurrentPercentage();

                if (currentPercentage > minPercentage) {
                    fill(currentPlayerColor);
                    Main.onBoxFilled(row, column);
                } else {
                    fill(whiteColor);
                    Main.onBoxReset(row, column);
                }
            }
        }
    }

    private void draw(double x1, double y1, double x2, double y2, Color color) {
        gc.strokeLine(x1, y1, x2, y2);
        gc.setStroke(color);

        previousX = x2;
        previousY = y2;
    }

    // Fills the box with given color
    private void fill(Color color) {
        gc.setFill(color);
        gc.fillRect(0, 0, boxWidth, boxHeight);
    }

    // Fills the box with white color
    public void reset() {
        gc.setFill(whiteColor);
        gc.fillRect(0, 0, boxWidth, boxHeight);
    }

    // Puts a cross in the box indicating it's being drawn by some other player
    public void reserve(Color color) {
        gc.setLineWidth(15); // Temporarily set line width to 15 for the cross
        gc.setStroke(color);
        gc.strokeLine(0, 0, boxWidth, boxHeight);
        gc.strokeLine(0, boxHeight, boxWidth, 0);
        gc.setLineWidth(lineWidth); // Set line width to original
    }

    // Forces the current player to stop drawing and fills the box with given color
    public void forceFill(Color color) {
        gc.setFill(color);
        gc.fillRect(0, 0, boxWidth, boxHeight);

        drawing = false;
        previousX = -1;
        previousY = -1;
    }

    // Calculates percentage of non-white pixels in the box
    // Returns 0 to 100
    private double getCurrentPercentage() {
        WritableImage image = this.snapshot(new SnapshotParameters(), null);
        PixelReader reader = image.getPixelReader();

        int coloredPixelCount = 0;

        for (int x = 0; x < boxWidth; x++) {
            for (int y = 0; y < boxHeight; y++) {
                Color pixel = reader.getColor(x, y);

                if (!pixel.equals(whiteColor)) {
                    coloredPixelCount++;
                }
            }
        }

        double percentage = coloredPixelCount * 100 / (boxWidth * boxHeight);

        return percentage;
    }
}
