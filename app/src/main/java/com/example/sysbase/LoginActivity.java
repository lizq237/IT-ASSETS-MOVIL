package com.example.sysbase;

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

            editEmail= findViewById(R.id.editEmail);
            editContraseña= findViewById(R.id.editContraseña);
            btnIniciarSesion= findViewById(R.id.btnIniciarSesion);

            btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Extraemos el texto de los campos de email y contra
                    String email= editEmail.getText().toString().trim();
                    String contraseña= editContraseña.getText().toString().trim();
                    if (email.isEmpty() || contraseña.isEmpty()){
                        Toast.makeText(LoginActivity.this, "Llena todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    iniciarSesionAPI(email,contraseña);
                }
            });

    }
    private void iniciarSesionAPI(String email, String password) {
        // en lugar de web, el devide es movil
        LoginRequest request = new LoginRequest(email, password, "mobile");

        // peticion asíncrona usando Retrofit
        RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // si es correcto, laravel nos regresa el rol y user
                    String nombreUsuario = response.body().getUser().getName();
                    String rol = response.body().getUser().getRole();
                    String token = response.body().getAccessToken();

                    Toast.makeText(LoginActivity.this, "Bienvenido " + nombreUsuario, Toast.LENGTH_LONG).show();

                } else {
                    // credenciales incorrectas
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // fallos de red
                Toast.makeText(LoginActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}