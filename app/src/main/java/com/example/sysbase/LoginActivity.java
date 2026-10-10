package com.example.sysbase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText editEmail;
    private EditText editContraseña;
    private Button btnIniciarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editEmail = findViewById(R.id.editEmail);
        editContraseña = findViewById(R.id.editContraseña);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);

        //valida los campos y llame a Laravel
        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString().trim();
                String password = editContraseña.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                iniciarSesionAPI(email, password);
            }
        });
    }

    private void iniciarSesionAPI(String email, String password) {
        LoginRequest request = new LoginRequest(email, password, "mobile");

        // Petición asíncrona usando Retrofit
        RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String nombreUsuario = response.body().getUser().getName();
                    String rol = response.body().getUser().getRole();
                    String token = response.body().getAccessToken();

                    // Guarda el token, el nombre del usuario y rol en SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                    prefs.edit()
                            .putString("TOKEN", token)
                            .putString("USER_NAME", nombreUsuario) // <-- Nombre guardado correctamente
                            .putString("USER_ROLE", rol) //guardamos el rol para ver que tipo de empleado esta entrando y darle sentido a las pantallas
                            .apply();

                    Toast.makeText(LoginActivity.this, "Bienvenido " + nombreUsuario, Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, EmployeeHomeActivity.class);
                    startActivity(intent);
                    finish();

                } else {

                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

                Toast.makeText(LoginActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}