package com.example.myapplication.controllers;

import android.util.Log;

import com.example.myapplication.models.PointCoord;
import com.example.myapplication.models.Puzzle;
import com.example.myapplication.models.PuzzlePair;
import com.example.myapplication.views.PuzzleView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Contrôleur principal du puzzle : gère la logique de tracé des chemins,
 * l'occupation de la grille, les annulations, et la détection de complétion.
 */
public class PuzzleController {

    private static final String TAG = "PuzzleController";

    private Puzzle puzzle; // le puzzle en cours
    private int[][] gridOccupation; // occupation de la grille (pairId ou -1)
    private boolean gameFinished; // drapeau : puzzle complété
    private PuzzleCompletionListener completionListener; // callback quand puzzle terminé

    private Map<Integer, List<PointCoord>> pathsByPair = new HashMap<>(); // chemins tracés
    private int currentPairId = -1; // paire en cours de tracé
    private List<PointCoord> currentPath = null; // chemin actuel

    private PuzzleView puzzleView; // vue associée
    private Stack<Move> moveHistory = new Stack<>(); // pile pour annuler

    /**
     * Constructeur du contrôleur.
     * Initialise la grille, les chemins et les têtes.
     */
    public PuzzleController(Puzzle puzzle, PuzzleView puzzleView, boolean isAchromate) {
        this.puzzle = puzzle;
        this.puzzleView = puzzleView;
        int size = puzzle.getSize();

        // Initialisation de la grille à -1 (vide)
        gridOccupation = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                gridOccupation[r][c] = -1;
            }
        }

        // Marquer les têtes de chaque paire dans la grille
        for (PuzzlePair pair : puzzle.getPairs()) {
            int pid = pair.getPairId();
            PointCoord f = pair.getFirst();
            PointCoord s = pair.getSecond();

            gridOccupation[f.getRow()][f.getCol()] = pid;
            gridOccupation[s.getRow()][s.getCol()] = pid;

            pathsByPair.put(pid, new ArrayList<>());
        }

        gameFinished = false;

        // Affiche la grille avec les chemins vides
        puzzleView.setDataForRendering(puzzle, gridOccupation, isAchromate, pathsByPair);
    }

    // Permet d'enregistrer un listener qui sera notifié quand le puzzle est terminé
    public void setPuzzleCompletionListener(PuzzleCompletionListener listener) {
        this.completionListener = listener;
    }

    // Met à jour l'affichage en fonction du mode achromate
    public void updateAchromate(boolean isAchromate) {
        puzzleView.setDataForRendering(puzzle, gridOccupation, isAchromate, pathsByPair);
    }

    /**
     * Appelé quand l'utilisateur touche l'écran pour commencer un tracé.
     */
    public void onTouchDown(float x, float y) {
        if (gameFinished) return;

        int row = puzzleView.pixelToRow(y);
        int col = puzzleView.pixelToCol(x);
        Log.d(TAG, "onTouchDown => r=" + row + ", c=" + col);

        if (!inBounds(row, col)) {
            currentPairId = -1;
            currentPath = null;
            return;
        }

        int occupant = gridOccupation[row][col];
        if (occupant == -1) {
            // Case vide : pas de tracé
            currentPairId = -1;
            currentPath = null;
        } else {
            // On touche une case occupée (tête ou chemin)
            if (currentPairId != -1 && currentPairId != occupant && currentPath != null) {
                cancelCurrentPath();
            }

            currentPairId = occupant;
            currentPath = pathsByPair.get(occupant);

            if (isBasePoint(row, col, occupant)) {
                // Si on clique sur la tête : on recommence le tracé
                removePathForPair(occupant);
                currentPath.clear();
                currentPath.add(new PointCoord(row, col));
            } else {
                int idx = indexOfCell(currentPath, row, col);
                if (idx == -1) {
                    currentPath.add(new PointCoord(row, col));
                } else {
                    removeCellsAfterIndex(currentPath, idx, occupant);
                }
            }
        }
        redraw();
    }

    /**
     * Appelé quand l'utilisateur fait glisser son doigt.
     */
    public void onTouchMove(float x, float y) {
        if (gameFinished || currentPairId == -1 || currentPath == null) return;

        int row = puzzleView.pixelToRow(y);
        int col = puzzleView.pixelToCol(x);

        if (!inBounds(row, col)) {
            cancelCurrentPath();
            redraw();
            return;
        }

        PointCoord lastCell = currentPath.get(currentPath.size() - 1);
        int dr = Math.abs(row - lastCell.getRow());
        int dc = Math.abs(col - lastCell.getCol());

        if (dr + dc == 0) return; // même case
        if (dr + dc == 1) { // case voisine (4 directions uniquement)
            int occupant = gridOccupation[row][col];

            if (isBasePoint(row, col, currentPairId) && !containsCell(currentPath, new PointCoord(row, col))) {
                // On atteint la 2e tête → chemin complété
                gridOccupation[row][col] = currentPairId;
                currentPath.add(new PointCoord(row, col));
                redraw();
                moveHistory.push(new Move(currentPairId, new ArrayList<>(currentPath)));
                checkIfPuzzleComplete();
                currentPairId = -1;
                currentPath = null;
                return;
            } else if (isBasePoint(row, col, occupant) && occupant != currentPairId) {
                // Touche la tête d'une autre paire
                cancelCurrentPath();
                redraw();
                return;
            }

            if (occupant == -1) {
                gridOccupation[row][col] = currentPairId;
                currentPath.add(new PointCoord(row, col));
            } else if (occupant == currentPairId) {
                int idx = indexOfCell(currentPath, row, col);
                if (idx != -1) {
                    removeCellsAfterIndex(currentPath, idx, currentPairId);
                }
            } else {
                cancelCurrentPath();
            }
        } else {
            // Pas une case adjacente : chemin annulé
            cancelCurrentPath();
        }
        redraw();
    }

    /**
     * Appelé quand l'utilisateur lève le doigt après avoir tracé.
     */
    public void onTouchUp(float x, float y) {
        if (gameFinished || currentPairId == -1 || currentPath == null) return;
        moveHistory.push(new Move(currentPairId, new ArrayList<>(currentPath)));
        checkIfPuzzleComplete();
        currentPairId = -1;
        currentPath = null;
    }


    /**
     * Vérifie si toutes les cases sont remplies et les têtes reliées.
     */
    private void checkIfPuzzleComplete() {
        if (!allPairsHaveBothHeads()) return;

        gameFinished = true;

        if (completionListener != null) {
            completionListener.onPuzzleCompleted();
        }
    }

    /**
     * Vérifie que tous les chemins relient bien leurs deux extrémités.
     */
    private boolean allPairsHaveBothHeads() {
        for (PuzzlePair pair : puzzle.getPairs()) {
            int pid = pair.getPairId();
            List<PointCoord> path = pathsByPair.get(pid);
            if (!containsCell(path, pair.getFirst()) || !containsCell(path, pair.getSecond())) {
                return false;
            }
        }
        return true;
    }

    // Vérifie si un chemin contient une case
    private boolean containsCell(List<PointCoord> path, PointCoord cell) {
        for (PointCoord pc : path) {
            if (pc.getRow() == cell.getRow() && pc.getCol() == cell.getCol()) {
                return true;
            }
        }
        return false;
    }

    // Supprime un chemin (sauf les têtes)
    private void removePathForPair(int pairId) {
        List<PointCoord> path = pathsByPair.get(pairId);
        for (PointCoord pc : path) {
            if (!isBasePoint(pc.getRow(), pc.getCol(), pairId)) {
                gridOccupation[pc.getRow()][pc.getCol()] = -1;
            }
        }
        path.clear();
    }

    // Supprime toutes les cases après un certain index
    private void removeCellsAfterIndex(List<PointCoord> path, int idx, int pairId) {
        for (int i = path.size() - 1; i > idx; i--) {
            PointCoord pc = path.get(i);
            if (!isBasePoint(pc.getRow(), pc.getCol(), pairId)) {
                gridOccupation[pc.getRow()][pc.getCol()] = -1;
            }
            path.remove(i);
        }
    }

    // Annule le tracé en cours
    private void cancelCurrentPath() {
        if (currentPairId == -1 || currentPath == null) return;
        for (PointCoord pc : currentPath) {
            if (!isBasePoint(pc.getRow(), pc.getCol(), currentPairId)) {
                gridOccupation[pc.getRow()][pc.getCol()] = -1;
            }
        }
        currentPath.clear();
        currentPairId = -1;
        currentPath = null;
    }

    // Trouve l'index d'une case dans un chemin
    private int indexOfCell(List<PointCoord> path, int row, int col) {
        for (int i = 0; i < path.size(); i++) {
            PointCoord pc = path.get(i);
            if (pc.getRow() == row && pc.getCol() == col) return i;
        }
        return -1;
    }

    // Vérifie si une case est une des deux têtes d’une paire
    private boolean isBasePoint(int r, int c, int pairId) {
        for (PuzzlePair pair : puzzle.getPairs()) {
            if (pair.getPairId() == pairId) {
                PointCoord f = pair.getFirst();
                PointCoord s = pair.getSecond();
                return (f.getRow() == r && f.getCol() == c) || (s.getRow() == r && s.getCol() == c);
            }
        }
        return false;
    }

    private boolean inBounds(int r, int c) {
        return (r >= 0 && c >= 0 && r < puzzle.getSize() && c < puzzle.getSize());
    }

    private void redraw() {
        puzzleView.invalidate();
    }

    // Getters / setters pour la sauvegarde/restauration
    public int[][] getGridOccupation() {
        return gridOccupation;
    }

    public void setGridOccupation(int[][] occupation) {
        this.gridOccupation = occupation;
    }

    public Map<Integer, List<PointCoord>> getPathsByPair() {
        return pathsByPair;
    }

    public void setPathsByPair(Map<Integer, List<PointCoord>> paths) {
        this.pathsByPair = paths;
    }


    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished(boolean finished, Map<Integer, List<PointCoord>> paths) {
        this.gameFinished = finished;
        this.pathsByPair = paths;
        puzzleView.setDataForRendering(puzzle, gridOccupation, puzzleView.isAchromate(), paths);
    }
}
