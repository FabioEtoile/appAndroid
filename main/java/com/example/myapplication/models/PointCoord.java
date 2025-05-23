package com.example.myapplication.models;

import java.io.Serializable;

/**
 * Représente une position (ligne, colonne) dans la grille.
 */
public class PointCoord implements Serializable {
    private int row;
    private int col;

    public PointCoord(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "PointCoord{" + "row=" + row + ", col=" + col + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PointCoord)) return false;
        PointCoord other = (PointCoord) obj;
        return this.row == other.row && this.col == other.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}
