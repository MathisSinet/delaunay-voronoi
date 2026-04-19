package com.example.poc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import com.example.poc.utils.DBWStep;
import com.example.poc.utils.DelaunayBowyerWatson;
import com.example.poc.utils.Triangle;

public class MainController implements Initializable {
    @FXML
    private Canvas canvas;
    @FXML
    private Spinner<Integer> numPointsSpinner;
    @FXML
    private Label numPointsLabel;

    private int NUM_POINTS = 30;
    private static final int POINT_RADIUS = 5;
    private Point2D[] points = new Point2D[NUM_POINTS];
    private DelaunayBowyerWatson triangulation;
    private int stateid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser le spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 30, 30);
        numPointsSpinner.setValueFactory(valueFactory);
        numPointsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            NUM_POINTS = newVal;
            points = new Point2D[NUM_POINTS];
            numPointsLabel.setText(String.valueOf(newVal));
            onResetCanvas();
        });
        numPointsLabel.setText("30");
        
        onResetCanvas();
    }

    @FXML
    public void onResetCanvas() {
        resetTriangulation();
        onJumpToEnd();
    }

    @FXML
    public void onJumpToStart() {
        stateid = 0;
        drawCanvas();
    }

    @FXML
    public void onForward() {
        stateid = Math.min(stateid + 1, triangulation.getStepCount() - 1);
        drawCanvas();
    }

    @FXML
    public void onBackward() {
        stateid = Math.max(stateid - 1, 0);
        drawCanvas();
    }

    @FXML
    public void onJumpToEnd() {
        stateid = triangulation.getStepCount() - 1;
        drawCanvas();
    }

    private void generateRandomPoints() {
        Random random = new Random();
        for (int i = 0; i < NUM_POINTS; i++) {
            points[i] = new Point2D(
                (0.2 + 0.6 * random.nextDouble()) * canvas.getWidth(),
                (0.2 + 0.6 * random.nextDouble()) * canvas.getHeight()
            );
        }
    }

    private void drawPoint(GraphicsContext gc, Point2D point) {
        gc.fillOval(
            point.getX() - POINT_RADIUS,
            point.getY() - POINT_RADIUS,
            POINT_RADIUS * 2,
            POINT_RADIUS * 2
        );
    }

    private void drawEdge(GraphicsContext gc, Point2D p1, Point2D p2) {
        gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    private void drawTriangle(GraphicsContext gc, Triangle triangle) {
        drawEdge(gc, triangle.getP1(), triangle.getP2());
        drawEdge(gc, triangle.getP2(), triangle.getP3());
        drawEdge(gc, triangle.getP3(), triangle.getP1());
    }

    private void drawCircle(GraphicsContext gc, Point2D center, double radius) {
        gc.strokeOval(center.getX() - radius, center.getY() - radius, 2*radius, 2*radius);
    }

    private void resetTriangulation() {
        generateRandomPoints();

        triangulation = new DelaunayBowyerWatson(points);
        stateid = 0;
    }

    private void drawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        DBWStep step = triangulation.getStep(stateid);

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Cercles circonscrits
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (Triangle triangle: step.triangles) {
            drawCircle(gc, triangle.getCenter(), triangle.getRadius());;
        };

        // Triangles
        gc.setFill(Color.ORANGE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        for (Triangle triangle: step.triangles) {
            drawTriangle(gc, triangle);
            drawPoint(gc, triangle.getCenter());
        };
        
        // Supertriangle
        gc.setStroke(Color.GREEN);
        drawTriangle(gc, triangulation.getSupertriangle());

        // Sommets
        gc.setFill(Color.BLACK);
        for (Point2D point: step.visited) {
            drawPoint(gc, point);
        }

        // Triangles à supprimer
        if (step.badTriangles != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            for (Triangle badTriangle: step.badTriangles) {
                drawTriangle(gc, badTriangle);
            }
        }

        // Dernier point ajouté
        if (step.newPoint != null) {
            gc.setFill(Color.LIME);
            drawPoint(gc, step.newPoint);
        }
    }
}