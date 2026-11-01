package com.example.sysbase;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.AssetViewHolder> {

    private List<Asset> assetList;
    private OnReportClickListener reportClickListener;

    public interface OnReportClickListener {
        void onReportClick(Asset asset);
    }

    public AssetAdapter(List<Asset> assetList, OnReportClickListener listener) {
        this.assetList = assetList;
        this.reportClickListener = listener;
    }

    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.length() < 10) {
            return "Sin fecha";
        }

        String fechaCorta = fecha.substring(0, 10);
        String[] partes = fechaCorta.split("-");

        if (partes.length != 3) {
            return "Sin fecha";
        }

        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset, parent, false);
        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder holder, int position) {
        Asset asset = assetList.get(position);

        holder.txtModel.setText(asset.getCode() + " — " + asset.getModel());
        holder.txtType.setText("Categoría: " + asset.getType() + " · Serie: " + asset.getSerial_number());
        holder.txtDate.setText("Registrado: " + formatearFecha(asset.getCreated_at()));
        // Configuración del estado y colores dinámicos
        String status = asset.getStatus() != null ? asset.getStatus().toLowerCase() : "";
        holder.txtStatus.setText("Estado: " + asset.getStatus());

        if (status.equals("activo")) {
            holder.txtStatus.setTextColor(Color.parseColor("#2E7D32")); // Verde oscuro elegante
        } else if (status.equals("mantenimiento")) {
            holder.txtStatus.setTextColor(Color.parseColor("#ED6C02")); // Naranja
        } else {
            holder.txtStatus.setTextColor(Color.parseColor("#D32F2F")); // Rojo para baja u otros
        }

        holder.btnReport.setOnClickListener(v -> {
            if (reportClickListener != null) {
                reportClickListener.onReportClick(asset);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assetList != null ? assetList.size() : 0;
    }

    public static class AssetViewHolder extends RecyclerView.ViewHolder {
        TextView txtModel, txtType, txtDate, txtStatus;
        Button btnReport;

        public AssetViewHolder(@NonNull View itemView) {
            super(itemView);
            txtModel = itemView.findViewById(R.id.txtEquipmentModel);
            txtType = itemView.findViewById(R.id.txtEquipmentType);
            txtDate = itemView.findViewById(R.id.txtAssignedDate);
            txtStatus = itemView.findViewById(R.id.txtEquipmentStatus); // Mapeo del nuevo TextView
            btnReport = itemView.findViewById(R.id.btnReportIssueCard);
        }
    }
}