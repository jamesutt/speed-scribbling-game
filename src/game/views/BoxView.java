package game.views;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class BoxView extends Canvas {
    private int row;
    private int column;
    private boolean drawing = false;
    private double previousX = -1;
    private double previousY = -1;
    private double width;
    private double height;
    private double minPercentage;
    private Paint color;

    private GraphicsContext gc;

    BoxView(double width, double height, double lineWidth, Paint color, double minPercentage) {
        super(width, height);

        this.width = width;
        this.height = height;
        this.minPercentage = minPercentage;
        this.color = color;

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);

        gc = this.getGraphicsContext2D();
        gc.setLineWidth(lineWidth);
        gc.setStroke(color);
    }

    private void handleMousePressed(MouseEvent e) {
        drawing = true;
        double x = e.getX();
        double y = e.getY();

        draw(x, y, x, y);
    }

    private void handleMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        if (drawing && previousX > -1 && previousY > -1 && x > 0 && x < width && y > 0 && y < height) {
            draw(previousX, previousY, x, y);
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        drawing = false;
        previousX = -1;
        previousY = -1;

        calculatePercentage();
    }

    private void draw(double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);

        previousX = x2;
        previousY = y2;
    }

    public void fill() {
        gc.setFill(color);
        gc.fillRect(0,0, width, height);
    }

    private void calculatePercentage() {
        WritableImage image = this.snapshot(new SnapshotParameters(), null);
        PixelReader reader = image.getPixelReader();
        Color white = Color.web("#FFF");

        int coloredPixelCount = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixel = reader.getColor(x, y);

                if (!pixel.equals(white)) {
                    coloredPixelCount++;
                }
            }
        }

        double percentage = coloredPixelCount * 100 / (width * height);
        System.out.println(percentage);

        if (percentage > minPercentage) {
            fill();
        }
    }
}
