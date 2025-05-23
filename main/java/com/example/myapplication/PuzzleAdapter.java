package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapplication.models.Puzzle;

import java.util.List;

/**
 * Adaptateur utilisé pour afficher chaque puzzle dans une ListView.
 * Il affiche le nom du puzzle, et le grise s'il est invalide.
 */
public class PuzzleAdapter extends ArrayAdapter<Puzzle> {

    private LayoutInflater inflater;

    public PuzzleAdapter(Context context, List<Puzzle> puzzles) {
        super(context, 0, puzzles);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Si la vue n'existe pas, on l'inflète à partir du fichier XML
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_puzzle, parent, false);
        }

        // Récupère le TextView dans la vue
        TextView puzzleName = convertView.findViewById(R.id.itemPuzzleName);

        // Récupère le puzzle courant
        Puzzle puzzle = getItem(position);

        // Affiche le nom du puzzle
        puzzleName.setText(puzzle.getName());

        // Si le puzzle est invalide, on change la couleur du texte en gris
        if (!puzzle.isValid()) {
            puzzleName.setTextColor(Color.GRAY);
        } else {
            puzzleName.setTextColor(Color.BLACK);
        }

        return convertView;
    }
}
