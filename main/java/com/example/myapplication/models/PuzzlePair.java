package com.example.myapplication.models;

import java.io.Serializable;

/**
 * Représente une paire de points à relier dans le puzzle.
 */
public class PuzzlePair implements Serializable {
    private PointCoord first;
    private PointCoord second;
    private int pairId;

    public PuzzlePair(PointCoord first, PointCoord second, int pairId) {
        this.first = first;
        this.second = second;
        this.pairId = pairId;
    }

    public PointCoord getFirst() {
        return first;
    }

    public PointCoord getSecond() {
        return second;
    }

    public int getPairId() {
        return pairId;
    }
}
