package com.example.poc.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

import javafx.geometry.Point2D;

public class Voronoi {
    List<Point2D> points;
    HashMap<Point2D, ArrayList<Triangle>> sortedTriangles;
    HashMap<Point2D, ArrayList<Edge>> polygons;
    Point2D corner;
    Point2D size;

    public HashMap<Point2D, ArrayList<Edge>> getPolygons() {
        return polygons;
    }

    /**
     * Génère le polygone de Voronoï d'un sommet donné
     * @param point Sommet (germe)
     * @param triangles Triangles adjacents à ce sommet
     * @return
     */
    private ArrayList<Edge> voronoiPolygon(Point2D point, ArrayList<Triangle> triangles) {
        int triangleCount = triangles.size();
        if (triangleCount == 0) return new ArrayList<>();

        // On trie tout d'abord les triangles
        // Pour cela, on crée un hashset des triangles associés à chaque point
        HashSet<Point2D> hasPredecessor = new HashSet<>();
        HashMap<Point2D, Triangle> triangleMap = new HashMap<>();
        // On remplit ces HashMap
        for (Triangle triangle: triangles) {
            Point2D p1 = triangle.nextVertex(point);
            Point2D p2 = triangle.nextVertex(p1);
            hasPredecessor.add(p2);
            triangleMap.put(p1, triangle);
        }
        // On cherche à repérer le début de la chaine
        Point2D start = null;
        for (Point2D pointWithSuccessor: triangleMap.keySet()) {
            if (!hasPredecessor.contains(pointWithSuccessor)) {
                start = pointWithSuccessor;
                break;
            }
        }
        
        // On crée maintenant le polygone
        ArrayList<Edge> edges = new ArrayList<>();
        // Cas 1 : cycle
        if (start == null) {
            start = triangles.getFirst().nextVertex(point);
            Point2D currentPoint = start;
            do {
                Triangle t1 = triangleMap.get(currentPoint);
                Point2D nextPoint = t1.nextVertex(currentPoint);
                Triangle t2 = triangleMap.get(nextPoint);
                edges.add(new Edge(t1.getCenter(), t2.getCenter()));
                currentPoint = nextPoint;
            }
            while (currentPoint != start);
        }

        // Cas 2 : solution temporaire !
        else {
            Triangle firstTriangle = triangleMap.get(start);
            Point2D firstMP = point.midpoint(start);
            Point2D vec = start.subtract(point);
            Point2D normal = new Point2D(vec.getY(), -vec.getX());
            Point2D farPoint = firstMP.add(normal.multiply(2000)); // Solution temporaire !!!
            edges.add(new Edge(farPoint, firstTriangle.getCenter()));

            Point2D currentPoint = start;
            while (true) {
                Triangle t1 = triangleMap.get(currentPoint);
                Point2D nextPoint = t1.nextVertex(currentPoint);
                Triangle t2 = triangleMap.get(nextPoint);
                if (t2 == null) break;
                edges.add(new Edge(t1.getCenter(), t2.getCenter()));
                currentPoint = nextPoint;
            }
        }

        return edges;
    }

    public Voronoi(Point2D[] points, List<Triangle> triangles, Point2D corner, Point2D size) {
        this.points = Arrays.asList(points);
        this.corner = corner;
        this.size = size;

        // Création de listes vides pour les triangles adjacents à chaque point
        sortedTriangles = new HashMap<>();
        polygons = new HashMap<>();
        for (Point2D point: points) {
            sortedTriangles.put(point, new ArrayList<>());
        }
        // Classement des triangles dans les tableaux de chaque points
        for (Triangle triangle: triangles) {
            sortedTriangles.get(triangle.getP1()).add(triangle);
            sortedTriangles.get(triangle.getP2()).add(triangle);
            sortedTriangles.get(triangle.getP3()).add(triangle);
        }
        // Génération des polygones de Voronoï
        for (Point2D point: points) {
            polygons.put(point, voronoiPolygon(point, sortedTriangles.get(point)));
        }
    }
}
