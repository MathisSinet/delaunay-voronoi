package com.example.poc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import com.example.poc.utils.DBWStep;
import com.example.poc.utils.DelaunayBowyerWatson;
import com.example.poc.utils.Edge;
import com.example.poc.utils.Triangle;
import com.example.poc.utils.Voronoi;

public class MainController implements Initializable {
    @FXML
    private Canvas canvas;
    @FXML
    private Spinner<Integer> numPointsSpinner;
    @FXML
    private Label numPointsLabel;
    @FXML
    private CheckBox drawCirclesCheckBox;
    @FXML
    private CheckBox drawDelaunayCheckBox;
    @FXML
    private CheckBox drawVoronoiCheckBox;

    private int NUM_POINTS = 10;
    private static final int POINT_RADIUS = 5;
    private static final int DELAUNAY_EDGE_WIDTH = 1;
    private static final int VORONOI_EDGE_WIDTH = 3;
    private Point2D[] points = new Point2D[NUM_POINTS];
    private DelaunayBowyerWatson triangulation;
    private int stateid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser le spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 50, 10);
        numPointsSpinner.setValueFactory(valueFactory);
        numPointsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            NUM_POINTS = newVal;
            points = new Point2D[NUM_POINTS];
            numPointsLabel.setText(String.valueOf(newVal));
            onResetCanvas();
        });
        numPointsLabel.setText("10");
        
        // Add listeners to checkboxes
        drawCirclesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> drawCanvas());
        drawDelaunayCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> drawCanvas());
        drawVoronoiCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> drawCanvas());
        
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
                (0.1 + 0.8 * random.nextDouble()) * canvas.getWidth(),
                (0.1 + 0.8 * random.nextDouble()) * canvas.getHeight()
            );
        }
    }

    private void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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
    
    private void drawEdge(GraphicsContext gc, Edge edge) {
        drawEdge(gc, edge.getV1(), edge.getV2());
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

        clearCanvas();

        if (stateid != triangulation.getStepCount() - 1) {
            drawDelaunayTriangulation();
        }
        else {
            if (isDrawDelaunayEnabled()) {
                drawDelaunayTriangulation();
            }
            if (isDrawVoronoiEnabled()) {
                drawVoronoi();
            }
            drawPoints();
        }
    }

    private void drawPoints() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        DBWStep step = triangulation.getStep(stateid);
        gc.setFill(Color.BLACK);
        for (Point2D point: step.visited) {
            drawPoint(gc, point);
        }
    }

    private void drawDelaunayTriangulation() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        DBWStep step = triangulation.getStep(stateid);

        // Cercles circonscrits
        if (isDrawCirclesEnabled()) {
            gc.setStroke(Color.LIGHTGRAY); // LIGHTGRAY
            gc.setLineWidth(1);
            for (Triangle triangle: step.triangles) {
                drawCircle(gc, triangle.getCenter(), triangle.getRadius());;
            };
        }

        // Triangles
        gc.setFill(Color.ORANGE); // ORANGE
        gc.setStroke(Color.BLUE); // BLUE
        gc.setLineWidth(DELAUNAY_EDGE_WIDTH);
        for (Triangle triangle: step.triangles) {
            drawTriangle(gc, triangle);
            drawPoint(gc, triangle.getCenter());
        };
        
        // Supertriangle
        gc.setStroke(Color.WHITE);
        drawTriangle(gc, triangulation.getSupertriangle());

        drawPoints();

        // Triangles à supprimer
        if (step.badTriangles != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(DELAUNAY_EDGE_WIDTH);
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

    private void drawVoronoi() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Voronoi voronoi = new Voronoi(
            points, 
            triangulation.getLastStep().triangles, 
            new Point2D(0, 0), 
            new Point2D(canvas.getWidth(), canvas.getHeight())
        );

        gc.setStroke(Color.PURPLE);
        gc.setLineWidth(VORONOI_EDGE_WIDTH);

        for (ArrayList<Edge> edges: voronoi.getPolygons().values()) {
            for (Edge edge: edges) {
                drawEdge(gc, edge);
            }
        }
    }

    public boolean isDrawCirclesEnabled() {
        return drawCirclesCheckBox.isSelected();
    }

    public boolean isDrawDelaunayEnabled() {
        return drawDelaunayCheckBox.isSelected();
    }

    public boolean isDrawVoronoiEnabled() {
        return drawVoronoiCheckBox.isSelected();
    }
}