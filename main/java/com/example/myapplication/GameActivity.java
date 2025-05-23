package com.example.myapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.controllers.PuzzleCompletionListener;
import com.example.myapplication.controllers.OnPuzzleTouchListener;
import com.example.myapplication.controllers.PuzzleController;
import com.example.myapplication.models.PointCoord;
import com.example.myapplication.models.Puzzle;
import com.example.myapplication.models.PuzzlePair;
import com.example.myapplication.utils.PuzzleParser;
import com.example.myapplication.views.PuzzleView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Activité qui gère l'affichage et l'interaction avec le puzzle sélectionné.
 * Elle affiche le puzzle, permet de tracer les chemins et gère le mode achromate.
 */
public class GameActivity extends Activity
        implements OnPuzzleTouchListener, PuzzleCompletionListener {

    private Puzzle puzzle;
    private PuzzleController puzzleController;
    private PuzzleView puzzleView;
    private TextView puzzleTitle;
    private boolean isAchromate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        puzzleTitle = findViewById(R.id.puzzleTitle);
        puzzleView = findViewById(R.id.puzzleView);

        SharedPreferences prefs = getSharedPreferences("MyApplicationPrefs", MODE_PRIVATE);
        isAchromate = prefs.getBoolean("isAchromateEnabled", false);

        String puzzleName;
        String assetFileName;

        if (savedInstanceState == null) {
            puzzleName = getIntent().getStringExtra("puzzleName");
            assetFileName = getIntent().getStringExtra("assetFileName");
            puzzle = PuzzleParser.parsePuzzle(this, assetFileName);
            puzzleController = new PuzzleController(puzzle, puzzleView, isAchromate);
        } else {
            puzzleName = savedInstanceState.getString("puzzleName");
            assetFileName = savedInstanceState.getString("assetFileName");
            puzzle = PuzzleParser.parsePuzzle(this, assetFileName);

            ArrayList<PuzzlePair> restoredPairs =
                    (ArrayList<PuzzlePair>) savedInstanceState.getSerializable("pairs");
            if (restoredPairs != null) {
                puzzle.getPairs().clear();
                puzzle.getPairs().addAll(restoredPairs);
            }

            int[][] savedGrid =
                    (int[][]) savedInstanceState.getSerializable("gridOccupation");
            Map<Integer, ArrayList<PointCoord>> savedPaths =
                    (HashMap<Integer, ArrayList<PointCoord>>) savedInstanceState.getSerializable("pathsByPair");
            boolean wasFinished = savedInstanceState.getBoolean("gameFinished", false);

            puzzleController = new PuzzleController(puzzle, puzzleView, isAchromate);
            puzzleController.setGridOccupation(savedGrid);
            puzzleController.setPathsByPair(new HashMap<>(savedPaths));
            puzzleController.setGameFinished(wasFinished, puzzleController.getPathsByPair());
        }

        puzzleView.setDataForRendering(
                puzzle,
                puzzleController.getGridOccupation(),
                isAchromate,
                puzzleController.getPathsByPair()
        );
        puzzleTitle.setText(puzzleName);

        if (!puzzle.isValid()) {
            Toast.makeText(this, "Puzzle invalide", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        puzzleController.setPuzzleCompletionListener(this);
        puzzleView.setOnPuzzleTouchListener(this);

        Button btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("MyApplicationPrefs", MODE_PRIVATE);
        isAchromate = prefs.getBoolean("isAchromateEnabled", false);
        puzzleController.updateAchromate(isAchromate);
        puzzleView.setDataForRendering(
                puzzle,
                puzzleController.getGridOccupation(),
                isAchromate,
                puzzleController.getPathsByPair()
        );
    }

    @Override
    public void onPuzzleTouchDown(float x, float y) {
        puzzleController.onTouchDown(x, y);
    }

    @Override
    public void onPuzzleTouchMove(float x, float y) {
        puzzleController.onTouchMove(x, y);
    }

    @Override
    public void onPuzzleTouchUp(float x, float y) {
        puzzleController.onTouchUp(x, y);
    }

    @Override
    public void onPuzzleCompleted() {
        Toast.makeText(this, "Bravo, puzzle résolu !", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("puzzleName", puzzle.getName());
        outState.putString("assetFileName", puzzle.getFileName());
        outState.putSerializable("gridOccupation", puzzleController.getGridOccupation());
        outState.putSerializable("pathsByPair", new HashMap<>(puzzleController.getPathsByPair()));
        outState.putSerializable("pairs", new ArrayList<>(puzzle.getPairs()));
        outState.putBoolean("gameFinished", puzzleController.isGameFinished());
    }
}
