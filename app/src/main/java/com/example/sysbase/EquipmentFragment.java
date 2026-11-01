package com.example.sysbase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EquipmentFragment extends Fragment {

    private RecyclerView recyclerViewAssets;
    private AssetAdapter assetAdapter;
    private ProgressBar progressBar;
    private TextView txtEmpty, txtError;
    private Button btnRetry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_equipment_fragment, container, false);

        // Vistas principales y de estado (asegúrate de tener estos IDs en tu XML)
        recyclerViewAssets = view.findViewById(R.id.recyclerViewAssets);
        progressBar = view.findViewById(R.id.progressBar); // Indicador de carga
        txtEmpty = view.findViewById(R.id.txtEmpty);       // Mensaje de lista vacía
        txtError = view.findViewById(R.id.txtError);       // Mensaje de error
        btnRetry = view.findViewById(R.id.btnRetry);       // Botón de reintentar

        recyclerViewAssets.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarEquiposAsignados();

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> cargarEquiposAsignados());
        }

        return view;
    }

    private void cargarEquiposAsignados() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN", "");

        if (token.isEmpty()) {
            volverAlLogin();
            return;
        }

        // 1. Mostrar estado de carga y ocultar vistas anteriores
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        recyclerViewAssets.setVisibility(View.GONE);
        if (txtEmpty != null) txtEmpty.setVisibility(View.GONE);
        if (txtError != null) txtError.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Asset>> call = apiService.getEmployeeAssets("Bearer " + token);

        call.enqueue(new Callback<List<Asset>>() {
            @Override
            public void onResponse(@NonNull Call<List<Asset>> call, @NonNull Response<List<Asset>> response) {
                if (!isAdded()) return; // Evita errores si el fragmento ya no está activo
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Asset> assets = response.body();

                    if (assets.isEmpty()) {
                        // 2. Manejo de lista vacía requerida por la Tarjeta #97
                        if (txtEmpty != null) {
                            txtEmpty.setText("No tienes equipos asignados");
                            txtEmpty.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // 3. Mostrar lista de equipos con éxito
                        assetAdapter = new AssetAdapter(assets, asset -> {
                            Intent intent = new Intent(getActivity(), AddTicketActivity.class);
                            intent.putExtra("ASSET_ID", asset.getId());
                            startActivity(intent);
                        });
                        recyclerViewAssets.setAdapter(assetAdapter);
                        recyclerViewAssets.setVisibility(View.VISIBLE);
                    }
                } else {
                    // 4. Manejo de códigos de error HTTP específicos
                    int code = response.code();
                    if (code == 401) {
                        Toast.makeText(
                                requireContext(),
                                "Sesión expirada. Inicia sesión nuevamente.",
                                Toast.LENGTH_LONG
                        ).show();

                        volverAlLogin();
                        return;
                    } else if (code == 403) {
                        if (txtError != null) {
                            txtError.setText("No tienes permisos o organización asignada.");
                            txtError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (txtError != null) {
                            txtError.setText("Error en el servidor (" + code + ").");
                            txtError.setVisibility(View.VISIBLE);
                        }
                    }
                    if (btnRetry != null) btnRetry.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Asset>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                // 5. Manejo de errores de red con opción de reintentar
                if (txtError != null) {
                    txtError.setText("Error de conexión. Verifica tu red.");
                    txtError.setVisibility(View.VISIBLE);
                }
                if (btnRetry != null) btnRetry.setVisibility(View.VISIBLE);
            }
        });
    }
    private void volverAlLogin() {
        requireActivity()
                .getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}