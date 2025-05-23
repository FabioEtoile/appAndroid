package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un puzzle : son nom, sa taille, ses paires et son état de validité.
 */
public class Puzzle {
    private String name;
    private int size;
    private boolean valid;
    private List<PuzzlePair> pairs;
    private String fileName;

    public Puzzle(String name, int size) {
        this.name = name;
        this.size = size;
        this.valid = true;
        this.pairs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<PuzzlePair> getPairs() {
        return pairs;
    }

    public void addPair(PuzzlePair pair) {
        pairs.add(pair);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
