package com.example.myapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

/**
 * Activité qui permet à l'utilisateur d'activer le mode achromate (affichage en nuances de gris).
 */
public class SettingsActivity extends Activity {

    private CheckBox achromateCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        achromateCheckBox = findViewById(R.id.achromateCheckBox);

        // Chargement de la préférence enregistrée
        SharedPreferences prefs = getSharedPreferences("MyApplicationPrefs", MODE_PRIVATE);
        boolean achromate = prefs.getBoolean("isAchromateEnabled", false);
        achromateCheckBox.setChecked(achromate);
    }

    /**
     * Appelé quand l'utilisateur clique sur "Enregistrer".
     * Sauvegarde l'état de la case à cocher dans les préférences.
     */
    public void onSaveSettings(View v) {
        boolean isChecked = achromateCheckBox.isChecked();
        SharedPreferences prefs = getSharedPreferences("MyApplicationPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isAchromateEnabled", isChecked);
        editor.apply();
        finish();
    }
}
