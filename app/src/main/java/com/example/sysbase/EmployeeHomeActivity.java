package com.example.sysbase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeHomeActivity extends AppCompatActivity {

    private FloatingActionButton fabAddEquipment;
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerViewAssets;
    private AssetAdapter assetAdapter;

    // Vistas de la sección de perfil
    private View layoutProfileContainer;
    private TextView tvUserName;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_home);

        // Inicializar vistas principales
        fabAddEquipment = findViewById(R.id.fabAddEquipment);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        recyclerViewAssets = findViewById(R.id.recyclerViewAssets);

        // Inicializar vistas del perfil
        layoutProfileContainer = findViewById(R.id.layoutProfileContainer);
        tvUserName = findViewById(R.id.tvUserName);
        btnLogout = findViewById(R.id.btnLogout);


        recyclerViewAssets.setLayoutManager(new LinearLayoutManager(this));

        // accion botón flotante para abrir el formulario de tickets
        fabAddEquipment.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeHomeActivity.this, AddTicketActivity.class);
            startActivity(intent);
        });

        // navegación de la barra inferior
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_equipment) {
                    mostrarVistaEquipos();
                    return true;
                } else if (id == R.id.nav_tickets) {
                    mostrarVistaTickets();
                    return true;
                } else if (id == R.id.nav_profile) {
                    mostrarVistaPerfil();
                    return true;
                }
                return false;
            }
        });

        // botón Log Out
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(EmployeeHomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }


        cargarEquiposAsignados();
    }

    private void mostrarVistaEquipos() {
        if (layoutProfileContainer != null) layoutProfileContainer.setVisibility(View.GONE);
        recyclerViewAssets.setVisibility(View.VISIBLE);
        fabAddEquipment.show();
    }

    private void mostrarVistaTickets() {
        if (layoutProfileContainer != null) layoutProfileContainer.setVisibility(View.GONE);
        recyclerViewAssets.setVisibility(View.GONE);
        fabAddEquipment.hide();
        Toast.makeText(this, "My Tickets: No hay tickets por el momento", Toast.LENGTH_SHORT).show();
    }

    private void mostrarVistaPerfil() {
        recyclerViewAssets.setVisibility(View.GONE);
        fabAddEquipment.hide();

        if (layoutProfileContainer != null) {
            layoutProfileContainer.setVisibility(View.VISIBLE);
        }

        // Carga el nombre del usuario desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userName = prefs.getString("USER_NAME", "Usuario");

        if (tvUserName != null) {
            tvUserName.setText("¡Hola, " + userName + "!");
        }
    }

    private void cargarEquiposAsignados() {
        // Recuperar el token Bearer guardado durante el inicio de sesión
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String token = prefs.getString("TOKEN", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamada a la API mediante Retrofit
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Asset>> call = apiService.getEmployeeAssets("Bearer " + token);

        call.enqueue(new Callback<List<Asset>>() {
            @Override
            public void onResponse(Call<List<Asset>> call, Response<List<Asset>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Asset> assetList = response.body();

                    // Configurar el adaptador con la lista de equipos reales
                    assetAdapter = new AssetAdapter(assetList, asset -> {
                        Intent intent = new Intent(EmployeeHomeActivity.this, AddTicketActivity.class);
                        intent.putExtra("ASSET_ID", asset.getId());
                        startActivity(intent);
                    });

                    recyclerViewAssets.setAdapter(assetAdapter);
                } else {
                    Toast.makeText(EmployeeHomeActivity.this, "No se pudieron cargar los equipos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Asset>> call, Throwable t) {
                Toast.makeText(EmployeeHomeActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}