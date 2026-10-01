package com.example.sysbase;

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

    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset, parent, false);
        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder holder, int position) {
        Asset asset = assetList.get(position);
        holder.txtModel.setText(asset.getModel());
        holder.txtType.setText(asset.getType() + " · " + asset.getSerial_number());
        holder.txtDate.setText("Asignado: " + asset.getCreated_at());

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
        TextView txtModel, txtType, txtDate;
        Button btnReport;

        public AssetViewHolder(@NonNull View itemView) {
            super(itemView);
            txtModel = itemView.findViewById(R.id.txtEquipmentModel);
            txtType = itemView.findViewById(R.id.txtEquipmentType);
            txtDate = itemView.findViewById(R.id.txtAssignedDate);
            btnReport = itemView.findViewById(R.id.btnReportIssueCard);
        }
    }
}