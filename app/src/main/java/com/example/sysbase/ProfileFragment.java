package com.example.sysbase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvUserName = view.findViewById(R.id.tvUserName);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Cargar nombre del usuario
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userName = prefs.getString("USER_NAME", "Usuario");
        tvUserName.setText("¡Hola, " + userName + "!");

        // Logica de cerrar sesión
        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply(); // Borrar sesión
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Cerrar actividad actual
        });

        return view;
    }
}