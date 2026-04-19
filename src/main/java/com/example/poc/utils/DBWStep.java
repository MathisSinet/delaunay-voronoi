package com.example.poc.utils;

import java.util.List;
import java.util.ArrayList;

import javafx.geometry.Point2D;

public class DBWStep {
    public Triangle supertriangle;
    public List<Triangle> triangles;
    public List<Triangle> badTriangles;
    public List<Point2D> visited;
    public Point2D newPoint;

    public DBWStep(Triangle supertriangle, List<Triangle> triangles, List<Triangle> badTriangles, List<Point2D> visited, Point2D newPoint) {
        this.supertriangle = supertriangle;
        this.triangles = new ArrayList<>(triangles);
        this.badTriangles = badTriangles == null ? null : new ArrayList<>(badTriangles);
        this.visited = new ArrayList<>(visited);
        this.newPoint = newPoint;
    }
}
