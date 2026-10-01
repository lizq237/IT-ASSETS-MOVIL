package com.example.sysbase;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTicketActivity extends AppCompatActivity {

    private EditText editAssetId, editDescription;
    private Button btnSubmitTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ticket);

        String assetIdReceived = getIntent().getStringExtra("ASSET_ID");

        editAssetId = findViewById(R.id.editAssetId);
        editDescription = findViewById(R.id.editDescription);
        btnSubmitTicket = findViewById(R.id.btnSubmitTicket);

        if (assetIdReceived != null && editAssetId != null) {
            editAssetId.setText(assetIdReceived);
        }

        btnSubmitTicket.setOnClickListener(v -> enviarTicket());
    }

    private void enviarTicket() {
        String assetIdStr = editAssetId.getText().toString().trim();
        String descriptionStr = editDescription.getText().toString().trim();

        if (assetIdStr.isEmpty() || descriptionStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar parámetros Multipart requeridos por la API
        RequestBody assetIdBody = RequestBody.create(MediaType.parse("text/plain"), assetIdStr);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), descriptionStr);

        // Token Bearer de la sesión actual
        String token = getSharedPreferences("UserSession", MODE_PRIVATE).getString("TOKEN", "");

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.createTicket("Bearer " + token, assetIdBody, descBody, null);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddTicketActivity.this, "¡Ticket enviado con éxito!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddTicketActivity.this, "Error al enviar el ticket" , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddTicketActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}