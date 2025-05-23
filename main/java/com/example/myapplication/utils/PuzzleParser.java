package com.example.myapplication.utils;

import android.content.Context;
import android.util.Log;
import com.example.myapplication.models.PointCoord;
import com.example.myapplication.models.Puzzle;
import com.example.myapplication.models.PuzzlePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire pour parser les fichiers XML de puzzles.
 * Chaque fichier décrit la taille de la grille et les paires de points.
 */
public class PuzzleParser {

    private static final String TAG = "PuzzleParser";

    public static Puzzle parsePuzzle(Context context, String assetFileName) {
        Puzzle puzzle = null;
        List<PointCoord> tempPoints = new ArrayList<>();
        int pairCounter = 0;

        try {
            InputStream input = context.getAssets().open("puzzles/" + assetFileName);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(input, null);

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tagName = parser.getName();
                        if ("puzzle".equals(tagName)) {
                            // Lecture des attributs du puzzle
                            String sizeAttr = parser.getAttributeValue(null, "size");
                            String nameAttr = parser.getAttributeValue(null, "nom");

                            if (sizeAttr == null) {
                                puzzle = new Puzzle(assetFileName.replace(".xml", ""), 0);
                                puzzle.setValid(false);
                            } else {
                                int sizeValue = Integer.parseInt(sizeAttr);
                                String puzzleName = (nameAttr != null) ? nameAttr : assetFileName.replace(".xml", "");
                                puzzle = new Puzzle(puzzleName, sizeValue);
                                puzzle.setFileName(assetFileName);

                                if (sizeValue < 5 || sizeValue > 14) {
                                    puzzle.setValid(false);
                                }
                            }

                        } else if ("point".equals(tagName)) {
                            String ligneAttr = parser.getAttributeValue(null, "ligne");
                            String colonneAttr = parser.getAttributeValue(null, "colonne");

                            if (ligneAttr == null || colonneAttr == null) {
                                if (puzzle != null) puzzle.setValid(false);
                            } else {
                                int row = Integer.parseInt(ligneAttr);
                                int col = Integer.parseInt(colonneAttr);
                                tempPoints.add(new PointCoord(row, col));
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("paire".equals(parser.getName())) {
                            if (puzzle != null && tempPoints.size() == 2) {
                                PuzzlePair pair = new PuzzlePair(tempPoints.get(0), tempPoints.get(1), pairCounter++);
                                puzzle.addPair(pair);
                            } else if (puzzle != null) {
                                puzzle.setValid(false);
                            }
                            tempPoints.clear();
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (Exception e) {
            if (puzzle == null) {
                puzzle = new Puzzle(assetFileName.replace(".xml", ""), 0);
                puzzle.setValid(false);
            }
        }

        if (puzzle == null) {
            puzzle = new Puzzle(assetFileName.replace(".xml", ""), 0);
            puzzle.setValid(false);
        }

        return puzzle;
    }
}
