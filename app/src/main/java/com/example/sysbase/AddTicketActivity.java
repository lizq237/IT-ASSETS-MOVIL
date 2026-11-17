package com.example.sysbase;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.ArrayAdapter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddTicketActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView editAssetId;
    private EditText editTitle, editDescription;
    private MaterialAutoCompleteTextView editPriority;
    private String selectedAssetId;
    private Button btnSubmitTicket, btnAttachPhoto, btnRemovePhoto;
    private ShapeableImageView ivImagePreview;
    private String assetIdReceived;

    // Uri para la fotografía seleccionada (Tarea #89 y #94)
    private Uri imageUri = null;
    private ActivityResultLauncher<android.content.Intent> pickMediaLauncher;

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

        // Referencias de UI para la evidencia (Tarea #89)
        btnAttachPhoto = findViewById(R.id.btnAttachPhoto);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        ivImagePreview = findViewById(R.id.ivImagePreview);

        configurarSelectorDeImagen();

        btnAttachPhoto.setOnClickListener(v -> abrirSelectorDeImagenes());
        btnRemovePhoto.setOnClickListener(v -> quitarImagenSeleccionada());
        btnSubmitTicket.setOnClickListener(v -> enviarTicket());

        cargarEquiposAsignados();
    }

    private void configurarSelectorDeImagen() {
        // Inicializar el selector de imágenes (Tarea #89)
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        if (imageUri != null) {
                            ivImagePreview.setImageURI(imageUri);
                            ivImagePreview.setVisibility(View.VISIBLE);
                            btnRemovePhoto.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    private void abrirSelectorDeImagenes() {
        // Usar intent para abrir el selector de documentos / fotos del sistema
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
        intent.setType("image/*");
        pickMediaLauncher.launch(intent);
    }

    private void quitarImagenSeleccionada() {
        imageUri = null;
        ivImagePreview.setImageURI(null);
        ivImagePreview.setVisibility(View.GONE);
        btnRemovePhoto.setVisibility(View.GONE);
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
        RequestBody reqAssetId = RequestBody.create(texto, selectedAssetId);
        RequestBody reqTitle = RequestBody.create(texto, titulo);
        RequestBody reqDescription = RequestBody.create(texto, descripcion);
        RequestBody reqPriority = RequestBody.create(texto, prioridad);

        // Preparar MultipartBody.Part para la imagen si existe (Tarea #94)
        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            try {
                File archivoTemp = crearArchivoTemporalDesdeUri(imageUri);
                if (archivoTemp != null) {
                    // Validar límite de 5 MiB (5242880 bytes) según la guía
                    if (archivoTemp.length() > 5242880) {
                        Toast.makeText(this, "La imagen supera el límite de 5 MiB.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String mimeType = getContentResolver().getType(imageUri);
                    if (mimeType == null) mimeType = "image/jpeg";

                    RequestBody reqFile = RequestBody.create(MediaType.parse(mimeType), archivoTemp);
                    imagePart = MultipartBody.Part.createFormData("image", archivoTemp.getName(), reqFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al procesar la imagen adjunta.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Call<TicketResponse> call = RetrofitClient.getApiService().createTicket(
                "Bearer " + token,
                reqAssetId,
                reqTitle,
                reqDescription,
                reqPriority,
                imagePart
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

    private File crearArchivoTemporalDesdeUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File archivoTemp = new File(getCacheDir(), "evidence_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(archivoTemp);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return archivoTemp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}