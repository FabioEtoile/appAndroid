package com.example.myapplication.controllers;

import com.example.myapplication.models.PointCoord;
import java.util.List;

/**
 * Représente un mouvement utilisateur, utilisé pour annuler (undo).
 */
public class Move {
    public int pairId;
    public List<PointCoord> path;

    public Move(int pairId, List<PointCoord> path) {
        this.pairId = pairId;
        this.path = path;
    }
}
