package mx.unam.fc.icat.focusmony.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import mx.unam.fc.icat.focusmony.MainActivity;
import mx.unam.fc.icat.focusmony.R;

/**
 * @author <a href= joshuahurtado@ciencias.unam.mx>  Joshua Abel Hurtado Aponte - @JoshuaJAHA</a>
 */

public class PreferencesActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Inicialización de la Toolbar
        Toolbar toolbar = findViewById(R.id.preferences_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_preferences);
        }

        // Inicializamos las preferencias por defecto
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Reemplazamos el contenido de la actividad con el fragmento
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_content, new PreferencesFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registramos el listener para detectar cambios mientras la actividad es visible
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desregistramos para evitar fugas de memoria.
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Callback que se dispara cuando el usuario cambia cualquier ajuste.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Caso Idioma
        if (key.equals(getString(R.string.lang_preference_key))) {
            String lang = sharedPreferences.getString(key, "es");
            applyLanguage(lang);
            // No llamamos a recreate() aquí directamente para evitar bucles con startActivity
        }

        // Caso Tema (Oscuro / Claro / Sistema)
        if (key.equals(getString(R.string.theme_preference_key))) {
            String themeValue = sharedPreferences.getString(key, "system");
            applyTheme(themeValue);
        }
    }

    /**
     * Implementación para cambiar la configuración del idioma
     */
    private void applyLanguage(String langCode) {
        java.util.Locale locale = new java.util.Locale(langCode);
        java.util.Locale.setDefault(locale);
        android.content.res.Resources resources = getResources();
        android.content.res.Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Reiniciamos la app desde el inicio para que el cambio sea global
        android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // El cambio tiene efecto y cerramos la actividad actual
    }

    /**
     * Aplica el modo oscuro o claro según la preferencia del usuario.
     * @param themeValue Valores esperados: "light", "dark" o "system".
     */
    private void applyTheme(String themeValue) {
        switch (themeValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                // Sigue la configuración del sistema operativo
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}