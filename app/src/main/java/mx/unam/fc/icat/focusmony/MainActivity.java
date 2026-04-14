package mx.unam.fc.icat.focusmony;

/**
 * @author <a href= joshuahurtado@ciencias.unam.mx>  Joshua Abel Hurtado Aponte - @JoshuaJAHA</a>
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect; //vibración
import android.os.Vibrator;       //vibración
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;      //mensaje

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog; //chips
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Random; //frase

// Base de Datos
import mx.unam.fc.icat.focusmony.model.Session;
import mx.unam.fc.icat.focusmony.model.SessionManager;
import mx.unam.fc.icat.focusmony.view.SessionHistoryActivity;

public class MainActivity extends AppCompatActivity {

    enum TimerState { IDLE, RUNNING, PAUSED }
    enum SessionMode { FOCUS, BREAK, REST}

    // milisegundos en 25, 5 y 15 min
    private static final long FOCUS_DURATION_MS   = 25 * 60 * 1000L;
    private static final long BREAK_DURATION_MS   =  5 * 60 * 1000L;
    private static final long REST_DURATION_MS    = 15 * 60 * 1000L;
    private static final int SESSIONS_BEFORE_REST = 4;

    //frases motivacionales
    private String[] frasesMotivacionales;

    private ChipGroup chipGroupMode;
    private Chip chipFocus, chipBreak, chipRest;
    private TextView tvTimerDisplay, tvSessionState, tvSessionsCompleted, tvMotivationalQuote;
    private MaterialButton btnStartStop, btnReset, btnSkip;
    private LinearLayout sessionDotsContainer;

    private CountDownTimer countDownTimer;
    private TimerState timerState = TimerState.IDLE;
    private SessionMode currentMode = SessionMode.FOCUS;
    private long timeLeftMillis = FOCUS_DURATION_MS;
    private int focusSessionsCompleted = 0;

    //declaramos el Gestor de Sesiones de SQLite
    private SessionManager sessionManager;

    private android.widget.ImageButton btnStats;
    private android.widget.ImageButton btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bindViews();

        //inicializamos la conexión a la base de datos
        sessionManager = new SessionManager(this);

        // Cargamos el arreglo de frases desde el diccionario
        frasesMotivacionales = getResources().getStringArray(R.array.motivational_quotes);

        //recupera los datos si se gira la pantalla
        if (savedInstanceState != null) {
            timeLeftMillis = savedInstanceState.getLong("timeLeft");
            currentMode = SessionMode.valueOf(savedInstanceState.getString("mode"));
            timerState = TimerState.valueOf(savedInstanceState.getString("state"));
            focusSessionsCompleted = savedInstanceState.getInt("completed");

            //si estaba corriendo lo pausamos al girar
            if (timerState == TimerState.RUNNING || timerState == TimerState.PAUSED) {
                timerState = TimerState.PAUSED;
                btnStartStop.setText(getString(R.string.btn_resume));
            }
        }

        setupClickListeners();
        updateTimerDisplay(timeLeftMillis);
    }

    //guarda los datos antes de girar la pantalla
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timeLeft", timeLeftMillis);
        outState.putString("mode", currentMode.name());
        outState.putString("state", timerState.name());
        outState.putInt("completed", focusSessionsCompleted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer(); //no deja que el reloj siga contando si cierras la app
    }

    private void bindViews() {
        chipGroupMode = findViewById(R.id.chipGroupMode);
        chipFocus = findViewById(R.id.chipFocus);
        chipBreak = findViewById(R.id.chipBreak);
        chipRest = findViewById(R.id.chipRest);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        btnStartStop = findViewById(R.id.btnStartStop);
        sessionDotsContainer = findViewById(R.id.sessionDotsContainer);
        tvSessionState = findViewById(R.id.tvSessionState);
        tvSessionsCompleted = findViewById(R.id.tvSessionsCompleted);
        btnReset = findViewById(R.id.btnReset);
        btnSkip = findViewById(R.id.btnSkip);
        tvMotivationalQuote = findViewById(R.id.tvMotivationalQuote);
        btnStats = findViewById(R.id.btnStats);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setupClickListeners() {
        btnStartStop.setOnClickListener(v -> {
            if (timerState == TimerState.RUNNING) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        //botones extra
        btnReset.setOnClickListener(v -> resetTimer());
        btnSkip.setOnClickListener(v -> skipToNextSession());

        // Navegación a la pantalla de Historial
        btnStats.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, SessionHistoryActivity.class);
            startActivity(intent);
        });

        //Navegación a la pantalla de Ajustes
        btnSettings.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, mx.unam.fc.icat.focusmony.view.PreferencesActivity.class);
            startActivity(intent);
        });

        //navegación por chips
        View.OnClickListener chipClickListener = v -> {
            SessionMode targetMode;
            if (v.getId() == R.id.chipBreak) targetMode = SessionMode.BREAK;
            else if (v.getId() == R.id.chipRest) targetMode = SessionMode.REST;
            else targetMode = SessionMode.FOCUS;

            if (currentMode != targetMode) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_mode_title))
                        .setMessage(getString(R.string.dialog_mode_message))
                        .setPositiveButton(getString(R.string.dialog_yes), (dialog, which) -> {
                            cancelTimer();
                            currentMode = targetMode;
                            timerState = TimerState.IDLE;
                            resetModeTime();
                            btnStartStop.setText(getString(R.string.btn_start));
                        })
                        .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> {
                            selectChipForMode(currentMode); //regresa la selección al actual
                        })
                        .show();
            } else {
                selectChipForMode(currentMode);
            }
        };

        chipFocus.setOnClickListener(chipClickListener);
        chipBreak.setOnClickListener(chipClickListener);
        chipRest.setOnClickListener(chipClickListener);
    }

    private void startTimer() {
        //tenemos que cancelar cualquier temporizador previo antes de iniciar uno nuevo
        cancelTimer();

        timerState = TimerState.RUNNING;
        btnStartStop.setText(getString(R.string.btn_pause));

        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                //terminó de forma natural
                onSessionFinished(true);
            }
        }.start();
    }

    private void pauseTimer() {
        cancelTimer();
        timerState = TimerState.PAUSED;
        btnStartStop.setText(getString(R.string.btn_resume));
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    //detener el tiempo, resetear el tiempo y actualizar la IU
    private void resetTimer() {
        cancelTimer();
        timerState = TimerState.IDLE;
        resetModeTime();
        btnStartStop.setText(getString(R.string.btn_start));
    }

    //cancelar el tiempo y forzar el fin, para saltar al siguiente
    private void skipToNextSession() {
        cancelTimer();
        //se forzó el final (interrumpida)
        onSessionFinished(false);
    }

    //modificamos el método para que reciba si fue exitosa o no
    private void onSessionFinished(boolean isCompleted) {
        timerState = TimerState.IDLE;

        //guardamos la sesión en SQLite ANTES de cambiar al siguiente modo
        saveCurrentSession(isCompleted);

        if (currentMode == SessionMode.FOCUS) {
            focusSessionsCompleted++;

            //llamamos al método para pintar el puntito de progreso
            addDot();

            if (focusSessionsCompleted >= SESSIONS_BEFORE_REST) {
                focusSessionsCompleted = 0;
                currentMode = SessionMode.REST;
                tvSessionState.setText(getString(R.string.mode_long_break));

                //limpiamos los puntitos porque ya empezamos un ciclo nuevo
                sessionDotsContainer.removeAllViews();
            } else {
                currentMode = SessionMode.BREAK;
                tvSessionState.setText(getString(R.string.mode_break));
            }
        } else {
            currentMode = SessionMode.FOCUS;
            tvSessionState.setText(getString(R.string.mode_focus));
        }

        tvSessionsCompleted.setText(getString(R.string.sessionsCount, focusSessionsCompleted));

        //mensaje al terminar cada sesion
        Toast.makeText(this, getString(R.string.toast_session_finished), Toast.LENGTH_SHORT).show();

        //vibracion simple
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            //vibra por 500 milisegundos
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        resetModeTime();
        btnStartStop.setText(getString(R.string.btn_start));
    }

    // método que empaqueta y envía los datos a la BD
    private void saveCurrentSession(boolean isCompleted) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());

        String date = dateFormat.format(calendar.getTime());
        String startTime = timeFormat.format(calendar.getTime());

        // Calculamos el nombre y los minutos según el modo que acaba de terminar
        String type;
        int duration;

        if (currentMode == SessionMode.FOCUS) {
            type = getString(R.string.mode_focus);
            duration = (int) (FOCUS_DURATION_MS / 60000);
        } else if (currentMode == SessionMode.BREAK) {
            type = getString(R.string.mode_break);
            duration = (int) (BREAK_DURATION_MS / 60000);
        } else {
            type = getString(R.string.mode_long_break);
            duration = (int) (REST_DURATION_MS / 60000);
        }

        Session newSession = new Session(type, date, startTime, duration, isCompleted);
        sessionManager.addSession(newSession);
    }

    //crear y agregar el puntito dinámicamente
    private void addDot() {
        View dot = new View(this);
        int dotSize = (int) getResources().getDimension(R.dimen.spacing_sm);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
        params.setMarginEnd((int) getResources().getDimension(R.dimen.spacing_xs));
        dot.setLayoutParams(params);
        dot.setBackground(ContextCompat.getDrawable(this, R.drawable.dot_session_completed));
        sessionDotsContainer.addView(dot);
    }

    private void resetModeTime() {
        if (currentMode == SessionMode.FOCUS) {
            timeLeftMillis = FOCUS_DURATION_MS;
            tvMotivationalQuote.setText(getString(R.string.quote_keep_going));
        } else if (currentMode == SessionMode.BREAK) {
            timeLeftMillis = BREAK_DURATION_MS;
            ponerFraseAleatoria();
        } else {
            timeLeftMillis = REST_DURATION_MS;
            ponerFraseAleatoria();
        }
        updateTimerDisplay(timeLeftMillis);
    }

    //método para poner la frase aleatoria
    private void ponerFraseAleatoria() {
        if (frasesMotivacionales != null && frasesMotivacionales.length > 0) {
            int randomNum = new Random().nextInt(frasesMotivacionales.length);
            tvMotivationalQuote.setText(frasesMotivacionales[randomNum]);
        }
    }

    private void updateTimerDisplay(long millis) {
        selectChipForMode(currentMode);
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        tvTimerDisplay.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void selectChipForMode(SessionMode mode) {
        int chipId;
        switch (mode) {
            case BREAK:
                chipId = R.id.chipBreak;
                highlightChip(chipBreak);
                break;
            case REST:
                chipId = R.id.chipRest;
                highlightChip(chipRest);
                break;
            default:
                chipId = R.id.chipFocus;
                highlightChip(chipFocus);
                break;
        }
        chipGroupMode.check(chipId);
    }

    private void highlightChip(Chip activeChip) {
        Chip[] allChips = {chipFocus, chipBreak, chipRest};

        for (Chip chip : allChips) {
            chip.setChipStrokeWidth(0); //quita el borde
        }

        //pone el borde al que está activo
        activeChip.setChipStrokeWidth(getResources().getDimension(R.dimen.chip_stroke_width));
        int colorAccent = ContextCompat.getColor(this, R.color.color_border_accent);
        activeChip.setChipStrokeColor(ColorStateList.valueOf(colorAccent));
    }

    //cambio de idioma
    @Override
    protected void attachBaseContext(Context newBase) {
        // Obtenemos las preferencias
        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(newBase);

        String lang = prefs.getString("mx.unam.icat.esie.focusmony.LANG_PREFERENCE_KEY", "es");

        java.util.Locale locale = new java.util.Locale(lang);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        super.attachBaseContext(newBase.createConfigurationContext(config));
    }
}