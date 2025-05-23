package com.example.myapplication.views;

// Import des classes nécessaires pour dessiner et gérer la vue
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.myapplication.models.Puzzle;
import com.example.myapplication.models.PuzzlePair;
import com.example.myapplication.models.PointCoord;
import com.example.myapplication.controllers.OnPuzzleTouchListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vue personnalisée qui dessine la grille du puzzle et les chemins entre les points.
 * - Fond gris foncé
 * - Cases gris clair espacées
 * - Chemins tracés entre les têtes
 * - Cercles affichés sur les têtes de paires
 */
public class PuzzleView extends View {

    private OnPuzzleTouchListener touchListener = null;

    // Marge extérieure autour de la grille
    private float paddingAroundPx = 48f;

    // Données du puzzle
    private Puzzle puzzle;
    private int[][] gridOccupation;
    private boolean isAchromate;
    private Map<Integer, List<PointCoord>> pathsByPair;

    // Outils pour dessiner
    private Paint backgroundPaint;
    private Paint cellPaint;
    private Paint linePaint;
    private Paint headPaint;

    // Tailles pour calculer les dimensions de la grille
    private float paddingPx;
    private float gridSizePx;
    private float cellSizePx;
    private float cellSpacing = 6f; // espacement entre les cases

    // Constructeurs (appelés par Android)
    public PuzzleView(Context context) {
        super(context);
        init(context);
    }

    public PuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Initialise les pinceaux et valeurs par défaut.
     */
    private void init(Context context) {
        // Fond sombre
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#2B2B2B"));

        // Couleur des cases
        cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setColor(Color.parseColor("#4A4A4A"));

        // Ligne de tracé des chemins
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(35f); // modifié dynamiquement ensuite
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        // Cercle des têtes
        headPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        headPaint.setStrokeWidth(2f);

        // Conversion de dp en pixels pour le padding intérieur (non utilisé ici)
        paddingPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Met à jour les données de la vue à afficher.
     */
    public void setDataForRendering(Puzzle puzzle,
                                    int[][] gridOccupation,
                                    boolean isAchromate,
                                    Map<Integer, List<PointCoord>> pathsByPair) {
        this.puzzle = puzzle;
        this.gridOccupation = gridOccupation;
        this.isAchromate = isAchromate;
        this.pathsByPair = pathsByPair;
        invalidate(); // force le redessin
    }

    /**
     * Enregistre un écouteur pour les interactions utilisateur.
     */
    public void setOnPuzzleTouchListener(OnPuzzleTouchListener listener) {
        this.touchListener = listener;
    }

    /**
     * Méthode principale de dessin appelée automatiquement par Android.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzle == null || gridOccupation == null) return;

        // Dessine le fond complet
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        int size = puzzle.getSize();

        // Calcul de l’espace disponible
        float gridWidth = getWidth() - 2 * paddingAroundPx;
        float gridHeight = getHeight() - 2 * paddingAroundPx;

        float cellWidth = gridWidth / size;
        float cellHeight = gridHeight / size;

        float offsetX = paddingAroundPx;
        float offsetY = paddingAroundPx;

        // Épaisseur du trait dynamique (proportionnelle à la case)
        linePaint.setStrokeWidth(Math.min(cellWidth, cellHeight) * 0.4f);

        // Dessine les cases de la grille
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                float left = offsetX + c * cellWidth + cellSpacing;
                float top = offsetY + r * cellHeight + cellSpacing;
                float right = offsetX + (c + 1) * cellWidth - cellSpacing;
                float bottom = offsetY + (r + 1) * cellHeight - cellSpacing;
                canvas.drawRect(left, top, right, bottom, cellPaint);
            }
        }

        // Attribution des couleurs pour chaque paire
        Map<Integer, Integer> colorMap = getColorMapping();

        // Dessine chaque paire
        for (PuzzlePair pair : puzzle.getPairs()) {
            int pairId = pair.getPairId();
            List<PointCoord> path = (pathsByPair != null) ? pathsByPair.get(pairId) : new ArrayList<>();

            // Couleur de cette paire
            int color = colorMap.getOrDefault(pairId, Color.WHITE);
            linePaint.setColor(color);
            headPaint.setColor(color);

            // Rayon des têtes (rond)
            float radius = Math.min(cellWidth, cellHeight) * 0.3f;

            // Dessine le premier point
            PointCoord first = pair.getFirst();
            float fcx = offsetX + (first.getCol() + 0.5f) * cellWidth;
            float fcy = offsetY + (first.getRow() + 0.5f) * cellHeight;
            canvas.drawCircle(fcx, fcy, radius, headPaint);

            // Dessine le second point
            PointCoord second = pair.getSecond();
            float scx = offsetX + (second.getCol() + 0.5f) * cellWidth;
            float scy = offsetY + (second.getRow() + 0.5f) * cellHeight;
            canvas.drawCircle(scx, scy, radius, headPaint);

            // Dessine les segments du chemin si tracé
            if (path != null && path.size() > 1) {
                for (int i = 1; i < path.size(); i++) {
                    PointCoord p0 = path.get(i - 1);
                    PointCoord p1 = path.get(i);

                    float cx0 = offsetX + (p0.getCol() + 0.5f) * cellWidth;
                    float cy0 = offsetY + (p0.getRow() + 0.5f) * cellHeight;
                    float cx1 = offsetX + (p1.getCol() + 0.5f) * cellWidth;
                    float cy1 = offsetY + (p1.getRow() + 0.5f) * cellHeight;

                    canvas.drawLine(cx0, cy0, cx1, cy1, linePaint);
                }
            }
        }
    }

    /**
     * Génère une couleur unique pour chaque paire, en fluo ou en niveau de gris.
     */
    private Map<Integer, Integer> getColorMapping() {
        Map<Integer, Integer> map = new HashMap<>();
        if (puzzle == null) return map;

        List<PuzzlePair> pairs = puzzle.getPairs();
        if (pairs == null) return map;

        for (PuzzlePair pair : pairs) {
            int id = pair.getPairId();

            int color;
            if (isAchromate) {
                // Nuances de gris en boucle
                int grayValue = 40 + (id * 30) % 200;
                color = Color.rgb(grayValue, grayValue, grayValue);
            } else {
                // Couleurs fluo dynamiques
                color = generateColorFromId(id);
            }

            map.put(id, color);
        }

        return map;
    }


    /**
     * Génère une couleur vive à partir d’un ID (fallback si palette épuisée).
     */
    private int generateColorFromId(int id) {
        float hue = (id * 47) % 360;
        float saturation = 0.7f;
        float lightness = 0.5f;
        return hslToRgb(hue, saturation, lightness);
    }

    /**
     * Convertit une couleur HSL en couleur RGB utilisable par Android.
     */
    private int hslToRgb(float h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = l - c / 2;

        float r = 0, g = 0, b = 0;
        if (h < 60)        { r = c; g = x; }
        else if (h < 120)  { r = x; g = c; }
        else if (h < 180)  { g = c; b = x; }
        else if (h < 240)  { g = x; b = c; }
        else if (h < 300)  { r = x; b = c; }
        else               { r = c; b = x; }

        int red   = Math.round((r + m) * 255);
        int green = Math.round((g + m) * 255);
        int blue  = Math.round((b + m) * 255);

        return Color.rgb(red, green, blue);
    }

    /**
     * Gère les interactions tactiles et les transmet au contrôleur.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchListener == null || puzzle == null) return super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchListener.onPuzzleTouchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchListener.onPuzzleTouchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchListener.onPuzzleTouchUp(x, y);
                break;
        }
        return true;
    }

    /**
     * Convertit une position Y en ligne de la grille.
     */
    public int pixelToRow(float y) {
        if (puzzle == null) return -1;
        int size = puzzle.getSize();
        float cellHeight = (getHeight() - 2 * paddingAroundPx) / size;
        float yAdj = y - paddingAroundPx;
        if (yAdj < 0) return -1;
        int row = (int) (yAdj / cellHeight);
        return (row >= size) ? -1 : row;
    }

    /**
     * Convertit une position X en colonne de la grille.
     */
    public int pixelToCol(float x) {
        if (puzzle == null) return -1;
        int size = puzzle.getSize();
        float cellWidth = (getWidth() - 2 * paddingAroundPx) / size;
        float xAdj = x - paddingAroundPx;
        if (xAdj < 0) return -1;
        int col = (int) (xAdj / cellWidth);
        return (col >= size) ? -1 : col;
    }

    public boolean isAchromate() {
        return isAchromate;
    }
}
