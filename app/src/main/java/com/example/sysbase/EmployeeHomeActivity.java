package com.example.sysbase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class EmployeeHomeActivity extends AppCompatActivity {

    private FloatingActionButton fabAddEquipment;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_home);

        fabAddEquipment = findViewById(R.id.fabAddEquipment);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        //leer el rol guardado en el Login
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "empleado");

        //cambiar título del menú según el rol
        Menu menu = bottomNavigation.getMenu();
        if (role.equals("admin_ti") || role.equals("tecnico")) {
            menu.findItem(R.id.nav_tickets).setTitle("Gestión de Reportes");
        } else {
            menu.findItem(R.id.nav_tickets).setTitle("Mis Tickets");
        }

        // btn flotante para reportar falla
        fabAddEquipment.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeHomeActivity.this, AddTicketActivity.class);
            startActivity(intent);
        });

        // navegación con Fragmentos
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_equipment) {
                    selectedFragment = new EquipmentFragment();
                    fabAddEquipment.show(); // El botón + solo se ve en esta pestaña
                } else if (id == R.id.nav_tickets) {
                    fabAddEquipment.hide();

                    // Lógica de roles para cargar los tickets correspondientes
                    if (role.equals("admin_ti") || role.equals("tecnico")) {
                        selectedFragment = new AllTicketsFragment();
                    } else {
                        selectedFragment = new MyTicketsFragment();
                    }
                } else if (id == R.id.nav_profile) {
                    fabAddEquipment.hide();
                    selectedFragment = new ProfileFragment();
                }

                // Inyectar el fragmento seleccionado en el FrameLayout
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayoutContainer, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        //Cargar la vista de equipos por defecto al abrir la app
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_equipment);
        }
    }
}