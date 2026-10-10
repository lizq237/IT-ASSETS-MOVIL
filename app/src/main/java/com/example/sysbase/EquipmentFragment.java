package com.example.sysbase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_equipment_fragment, container, false);

        recyclerViewAssets = view.findViewById(R.id.recyclerViewAssets);
        recyclerViewAssets.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarEquiposAsignados();

        return view;
    }

    private void cargarEquiposAsignados() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN", "");

        if (token.isEmpty()) return;

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Asset>> call = apiService.getEmployeeAssets("Bearer " + token);

        call.enqueue(new Callback<List<Asset>>() {
            @Override
            public void onResponse(Call<List<Asset>> call, Response<List<Asset>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    assetAdapter = new AssetAdapter(response.body(), asset -> {
                        Intent intent = new Intent(getActivity(), AddTicketActivity.class);
                        intent.putExtra("ASSET_ID", asset.getId());
                        startActivity(intent);
                    });
                    recyclerViewAssets.setAdapter(assetAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Asset>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}