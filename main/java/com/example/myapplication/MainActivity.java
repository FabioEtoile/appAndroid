package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.models.Puzzle;
import com.example.myapplication.utils.PuzzleParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité principale affichant la liste des puzzles disponibles.
 * L'utilisateur peut cliquer sur un puzzle valide pour démarrer une partie.
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private PuzzleAdapter adapter;
    private List<Puzzle> puzzleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sous-titre
        TextView subTitle = findViewById(R.id.subTitle);
        subTitle.setText("Choisissez un puzzle");

        listView = findViewById(R.id.puzzleListView);

        // Charge la liste de puzzles depuis les assets
        puzzleList = loadPuzzlesFromAssets();

        // Création et association de l'adaptateur
        adapter = new PuzzleAdapter(this, puzzleList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        // Bouton vers les paramètres
        Button buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Bouton pour quitter l'application
        Button buttonQuit = findViewById(R.id.buttonQuit);
        buttonQuit.setOnClickListener(v -> finish());
    }

    /**
     * Charge les puzzles présents dans le dossier assets/puzzles
     */
    private List<Puzzle> loadPuzzlesFromAssets() {
        List<Puzzle> list = new ArrayList<>();
        try {
            String[] files = getAssets().list("puzzles");
            if (files != null) {
                for (String file : files) {
                    Puzzle p = PuzzleParser.parsePuzzle(this, file);
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
        Puzzle p = puzzleList.get(position);

        // Affiche un message si le puzzle est invalide
        if (!p.isValid()) {
            Toast.makeText(this, "Ce puzzle est invalide et ne peut pas être joué.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lancement de l'activité de jeu avec les informations du puzzle
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("puzzleName", p.getName());
        String fileName = p.getFileName();
        intent.putExtra("assetFileName", (fileName != null) ? fileName : p.getName() + ".xml");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
