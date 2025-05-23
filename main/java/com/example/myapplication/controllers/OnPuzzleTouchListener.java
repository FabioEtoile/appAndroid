package com.example.myapplication.controllers;

/**
 * Interface permettant de capter les événements tactiles sur la grille.
 */
public interface OnPuzzleTouchListener {
    void onPuzzleTouchDown(float x, float y);
    void onPuzzleTouchMove(float x, float y);
    void onPuzzleTouchUp(float x, float y);
}
