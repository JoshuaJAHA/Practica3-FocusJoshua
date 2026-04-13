package mx.unam.fc.icat.focusmony.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mx.unam.fc.icat.focusmony.R;
import mx.unam.fc.icat.focusmony.model.Session;
import mx.unam.fc.icat.focusmony.model.SessionManager;

/**
 * @author <a href= joshuahurtado@ciencias.unam.mx>  Joshua Abel Hurtado Aponte - @JoshuaJAHA</a>
 */

public class SessionHistoryActivity extends AppCompatActivity {

    // Componentes de la Interfaz de Usuario.
    private Toolbar toolbar;
    private TextView tvResultCount;
    private View layoutEmpty;
    private RecyclerView recyclerView;

    // TODO: Declarar los componentes de filtrado (ChipGroup y Chips individuales).

    // Lógica y Datos.
    private SessionHistoryAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_session);

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterLogic();
        updateHistoryDisplay();
    }

    /**
     * Vincula las variables con los componentes del XML.
     */
    private void bindViews() {
        toolbar = findViewById(R.id.history_toolbar);
        tvResultCount = findViewById(R.id.tvResultCount);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        recyclerView = findViewById(R.id.recyclerViewHistory);

        // TODO: Vincular Chips mediante findViewById y asignar IDs correspondientes.

        sessionManager = new SessionManager(this);
    }

    /**
     * Configuración del sistema de filtrado por temporalidad.
     */
    private void setupFilterLogic() {
        // Encontramos el grupo de botones
        com.google.android.material.chip.ChipGroup chipGroup = findViewById(R.id.chipGroupFilter);

        // Listener para saber cuándo el usuario toca uno
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return; //Si no hay nada seleccionado, no hace nada

            int checkedId = checkedIds.get(0);

            // toda la historia de tu base de datos SQLite
            List<Session> allSessions = sessionManager.getHistory();
            List<Session> filteredSessions = new java.util.ArrayList<>();

            // sacamos la fecha del día de hoy en el mismo formato que guardamos (dd/MM/yyyy)
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            String today = sdf.format(new java.util.Date());

            // filtramos dependiendo del botón que se tocó
            if (checkedId == R.id.chipToday) {
                // Si tocó Hoy, revisamos y solo guardamos las que coincidan con la fecha de hoy
                for (Session s : allSessions) {
                    if (s.getDate().equals(today)) {
                        filteredSessions.add(s);
                    }
                }
            } else {
                // Si tocó Todas le mostramos todo el historial completo
                filteredSessions.addAll(allSessions);
            }

            // Actualizamos el adaptador para que la lista visual cambie
            adapter = new SessionHistoryAdapter(filteredSessions, getResources());
            recyclerView.setAdapter(adapter);

            // Actualizamos el texto que dice "X sesiones"
            int total = filteredSessions.size();
            tvResultCount.setText(total + (total == 1 ? " sesión" : " sesiones"));

            // Si el filtro da 0 resultados, mostramos el letrero de "Aún no hay sesiones"
            layoutEmpty.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(total == 0 ? View.GONE : View.VISIBLE);
        });
    }

    /**
     * Configura la Toolbar como ActionBar de la actividad.
     * Habilita el botón de retroceso (Up Navigation) y asigna el título
     * desde los recursos de cadena para soporte multi-idioma.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Historial de Sesiones");
        }
    }

    /**
     * Inicializa el RecyclerView con su LayoutManager y Adaptador.
     * Vincula la lista de sesiones obtenida del SessionManager con la
     * interfaz visual mediante el SessionHistoryAdapter.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtenemos los datos iniciales.
        List<Session> history = sessionManager.getHistory();

        // Inicializamos el adaptador.
        adapter = new SessionHistoryAdapter(history, getResources());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Gestiona la visibilidad de la UI y actualiza el contador.
     */
    private void updateHistoryDisplay() {
        // TODO: Recuperar datos reales para el listado de sesiones.

        List<Session> sessions = sessionManager.getHistory();
        boolean isEmpty = (sessions == null || sessions.isEmpty());

        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        // TODO: Investigar cómo usar Plurals en strings.xml para manejar "1 sesión" vs "2 sesiones".
        int total = sessions != null ? sessions.size() : 0;
        tvResultCount.setText(total + (total == 1 ? " sesión" : " sesiones"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}