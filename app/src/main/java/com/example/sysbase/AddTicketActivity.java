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
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.List;

public class AddTicketActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView editAssetId;
    private EditText editTitle, editDescription;
    private MaterialAutoCompleteTextView editPriority;
    private String selectedAssetId;
    private Button btnSubmitTicket;
    private String assetIdReceived;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ticket);

        assetIdReceived = getIntent().getStringExtra("ASSET_ID");

        editAssetId = findViewById(R.id.editAssetId);
        editDescription = findViewById(R.id.editDescription);
        editTitle = findViewById(R.id.editTitle);
        editPriority = findViewById(R.id.editPriority);
        btnSubmitTicket = findViewById(R.id.btnSubmitTicket);


        btnSubmitTicket.setOnClickListener(v -> enviarTicket());
        cargarEquiposAsignados();
    }

    private void cargarEquiposAsignados() {
        String token = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("TOKEN", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "Inicia sesión para consultar tus equipos.", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().getEmployeeAssets("Bearer " + token)
                .enqueue(new Callback<List<Asset>>() {
                    @Override
                    public void onResponse(Call<List<Asset>> call, Response<List<Asset>> response) {
                        if (isFinishing() || isDestroyed()) return;

                        List<Asset> equipos = response.body();
                        if (!response.isSuccessful() || equipos == null) {
                            Toast.makeText(AddTicketActivity.this,
                                    "No se pudieron cargar tus equipos (" + response.code() + ").",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (equipos.isEmpty()) {
                            editAssetId.setHint("No tienes equipos asignados");
                            editAssetId.setEnabled(false);
                            return;
                        }

                        List<String> etiquetas = new ArrayList<>();
                        for (Asset equipo : equipos) {
                            etiquetas.add(equipo.getCode() + " — " + equipo.getModel());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                AddTicketActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                etiquetas
                        );
                        editAssetId.setAdapter(adapter);

                        editAssetId.setOnItemClickListener((parent, view, posicion, id) -> {
                            selectedAssetId = equipos.get(posicion).getId();
                        });

                        // si se abre de una tarjeta, selecciona ese equipo directamente
                        for (int i = 0; i < equipos.size(); i++) {
                            if (assetIdReceived != null && assetIdReceived.equals(equipos.get(i).getId())) {
                                editAssetId.setText(etiquetas.get(i), false);
                                selectedAssetId = equipos.get(i).getId();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Asset>> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(AddTicketActivity.this,
                                "No se pudo conectar con la API.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void enviarTicket() {
        String titulo = editTitle.getText().toString().trim();
        String descripcion = editDescription.getText().toString().trim();
        String prioridad = editPriority.getText().toString().trim();

        if (selectedAssetId == null || selectedAssetId.isEmpty()) {
            Toast.makeText(this, "Selecciona un equipo asignado.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (titulo.isEmpty()) {
            editTitle.setError("Escribe el título del reporte.");
            editTitle.requestFocus();
            return;
        }
        if (descripcion.isEmpty()) {
            editDescription.setError("Describe el problema.");
            editDescription.requestFocus();
            return;
        }
        if (prioridad.isEmpty()) {
            Toast.makeText(this, "Selecciona una prioridad.", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = getSharedPreferences("UserSession", MODE_PRIVATE).getString("TOKEN", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "Inicia sesión para enviar el reporte.", Toast.LENGTH_LONG).show();
            return;
        }

        MediaType texto = MediaType.parse("text/plain");
        Call<TicketResponse> call = RetrofitClient.getApiService().createTicket(
                "Bearer " + token,
                RequestBody.create(texto, selectedAssetId),
                RequestBody.create(texto, titulo),
                RequestBody.create(texto, descripcion),
                RequestBody.create(texto, prioridad),
                null
        );

        btnSubmitTicket.setEnabled(false);
        call.enqueue(new Callback<TicketResponse>() {
            @Override
            public void onResponse(Call<TicketResponse> call, Response<TicketResponse> response) {
                if (isFinishing() || isDestroyed()) return;

                TicketResponse body = response.body();
                String id = body != null ? body.getTicketId() : null;

                if (response.code() == 201 && id != null && !id.isEmpty()) {
                    Toast.makeText(AddTicketActivity.this,
                            "Reporte registrado. Folio: " + id, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                btnSubmitTicket.setEnabled(true);
                String mensaje = "No se pudo confirmar el reporte (" + response.code() + ").";
                if (response.code() == 401) {
                    mensaje = "Tu sesión expiró. Inicia sesión nuevamente.";
                } else if (response.code() == 422) {
                    mensaje = "Revisa los campos y que el equipo siga asignado a tu cuenta.";
                }
                Toast.makeText(AddTicketActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<TicketResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;

                btnSubmitTicket.setEnabled(true);
                Toast.makeText(AddTicketActivity.this,
                        "No se pudo confirmar el envío. Revisa tu conexión.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}