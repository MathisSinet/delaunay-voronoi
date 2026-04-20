package com.example.poc.utils;

import javafx.geometry.Point2D;

public class Triangle {
    final Point2D p1, p2, p3;
    final Point2D center;
    final double radius;

    public static Triangle supertriangle(Point2D[] points) {
        final double EPSILON = 100;
        double xmin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        for (Point2D point: points) {
            xmin = Math.min(xmin, point.getX());
            xmax = Math.max(xmax, point.getX());
            ymin = Math.min(ymin, point.getY());
            ymax = Math.max(ymax, point.getY());
        }
        return new Triangle(
            new Point2D(xmin - (xmax - xmin), ymin - (ymax - ymin)),
            new Point2D(4*xmax - 3*xmin + 3*EPSILON, ymin - (ymax - ymin)),
            new Point2D(xmin - (xmax - xmin), 4*ymax - 3*ymin + 3*EPSILON)
        );
    }

    public Point2D getP1() {
        return p1;
    }

    public Point2D getP2() {
        return p2;
    }

    public Point2D getP3() {
        return p3;
    }

    public Point2D getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    private Point2D calculateCenter() {
        final Point2D vec1 = p2.subtract(p1);
        final Point2D vec2 = p3.subtract(p1);
        final Point2D mid1 = p1.midpoint(p2);
        final Point2D mid2 = p1.midpoint(p3);
        final double det = vec1.getX() * vec2.getY() - vec1.getY() * vec2.getX();
        if (det == 0.) {
            return p2.midpoint(p3);
        }
        final double A = vec1.dotProduct(mid1) / det;
        final double B = vec2.dotProduct(mid2) / det;
        return new Point2D(A * vec2.getY() - B * vec1.getY(), -A * vec2.getX() + B * vec1.getX());
    }

    public boolean hasEdge(Edge edge) {
        Point2D v1 = edge.getV1();
        Point2D v2 = edge.getV2();
        return (
            p1.equals(v1) && (p2.equals(v2) || p3.equals(v2)) ||
            p2.equals(v1) && (p3.equals(v2) || p1.equals(v2)) ||
            p3.equals(v1) && (p1.equals(v2) || p2.equals(v2))
        );
    }

    public Edge[] getEdges() {
        return new Edge[]{new Edge(p1, p2), new Edge(p2, p3), new Edge(p3, p1)};
    }

    public boolean shareVertex(Triangle triangle) {
        return p1.equals(triangle.getP1()) || p1.equals(triangle.getP2()) || p1.equals(triangle.getP3()) ||
            p2.equals(triangle.getP1()) || p2.equals(triangle.getP2()) || p2.equals(triangle.getP3()) ||
            p3.equals(triangle.getP1()) || p3.equals(triangle.getP2()) || p3.equals(triangle.getP3());
    }

    public Point2D nextVertex(Point2D vertex) {
        if (vertex.equals(p1)) {
            return p2;
        }
        if (vertex.equals(p2)) {
            return p3;
        }
        return p1;
    }
    public Point2D previousVertex(Point2D vertex) {
        if (vertex.equals(p1)) {
            return p3;
        }
        if (vertex.equals(p3)) {
            return p2;
        }
        return p1;
    }

    public Triangle(Point2D pt1, Point2D pt2, Point2D pt3) {
        if (pt1.angle(pt2, pt3) < 180) {
            p1 = pt1;
            p2 = pt2;
            p3 = pt3;
        }
        else {
            p1 = pt1;
            p2 = pt3;
            p3 = pt2;
        }
        center = calculateCenter();
        radius = center.distance(pt1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        };
        if (obj instanceof Triangle t) {
            return (
                p1.equals(t.getP1()) && p2.equals(t.getP2()) && p3.equals(t.getP3()) ||
                p1.equals(t.getP2()) && p2.equals(t.getP3()) && p3.equals(t.getP1()) ||
                p1.equals(t.getP3()) && p2.equals(t.getP1()) && p3.equals(t.getP2())
            );
        }
        return false;
    }
}
