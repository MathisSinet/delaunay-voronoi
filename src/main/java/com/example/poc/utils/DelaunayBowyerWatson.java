package com.example.poc.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.geometry.Point2D;

public class DelaunayBowyerWatson {
    List<Point2D> visited;
    List<Triangle> triangles;
    Triangle supertriangle;
    List<DBWStep> steps;

    public List<Triangle> getTriangles() {
        return triangles;
    }

    public Triangle getSupertriangle() {
        return supertriangle;
    }

    public DBWStep getStep(int id) {
        return steps.get(id);
    }

    public int getStepCount() {
        return steps.size();
    }

    public DelaunayBowyerWatson(Point2D[] points) {
        visited = new ArrayList<>();
        triangles = new ArrayList<>();
        steps = new ArrayList<>();
        supertriangle = Triangle.supertriangle(points);
        triangles.add(supertriangle);

        steps.add(new DBWStep(supertriangle, triangles, null, visited, null));

        for (Point2D point: points) {
            visited.add(point);

            // ETAPE 1 : Repérage des triangles à supprimer
            List<Triangle> badTriangles = new ArrayList<>();
            for (Triangle triangle: triangles) {
                // On vérifie si le nouveau point est dans le cercle circonscrit du triangle
                if (point.distance(triangle.getCenter()) < triangle.getRadius()) {
                    badTriangles.add(triangle);
                }
            }
            steps.add(new DBWStep(supertriangle, triangles, badTriangles, visited, point));
            // ETAPE 2: On détermine le polynôme englobant
            List<Edge> polygon = new ArrayList<>();
            // On regarde chaque triangle à supprimer
            for (Triangle badTriangle: badTriangles) {
                // On regarde chaque côté
                for (Edge edge: badTriangle.getEdges()) {
                    boolean toAdd = true;
                    // On regarde si ce côté est partagé par un autre triangle à supprimer
                    for (Triangle otherBadTriangle: badTriangles) {
                        if (!badTriangle.equals(otherBadTriangle) && otherBadTriangle.hasEdge(edge)) {
                            toAdd = false;
                            break;
                        }
                    }
                    // S'il est unique, alors il fait partie du polynôme englobant
                    if (toAdd) polygon.add(edge);
                }
            }
            // ETAPE 3: On supprime les triangles à supprimer
            Iterator<Triangle> iter = triangles.listIterator();
            while (iter.hasNext()) {
                Triangle triangle = iter.next();
                if (badTriangles.contains(triangle)) {
                    iter.remove();
                }
            }
            steps.add(new DBWStep(supertriangle, triangles, null, visited, point));
            // ETAPE 4: On triangule le trou polygonal
            for (Edge edge: polygon) {
                triangles.add(new Triangle(point, edge.getV1(), edge.getV2()));
            }
            steps.add(new DBWStep(supertriangle, triangles, null, visited, point));
        }
        // Nettoyage
        Iterator<Triangle> iter = triangles.listIterator();
        while (iter.hasNext()) {
            Triangle triangle = iter.next();
            if (triangle.shareVertex(supertriangle)) {
                iter.remove();
            }
        }
        steps.add(new DBWStep(supertriangle, triangles, null, visited, null));
    }
}
