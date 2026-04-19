package com.example.poc.utils;

import javafx.geometry.Point2D;

public class Edge {
    Point2D v1, v2;

    public Point2D getV1() {
        return v1;
    }

    public Point2D getV2() {
        return v2;
    }

    public Edge(Point2D v1, Point2D v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        };
        if (obj instanceof Edge e) {
            return (
                v1.equals(e.getV1()) && v2.equals(e.getV2()) ||
                v1.equals(e.getV2()) && v2.equals(e.getV1())
            );
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (v1.hashCode() >> 1) + (v2.hashCode() >> 1);
    }
}
